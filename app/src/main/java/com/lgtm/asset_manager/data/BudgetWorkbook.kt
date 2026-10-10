package com.lgtm.asset_manager.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.xml.parsers.DocumentBuilderFactory

data class WorkbookImport(
    val assets: List<AssetRecord>,
    val budget: List<ExpenseEntry>,
)

/** Minimal Office Open XML workbook with separate asset and budget worksheets. */
object BudgetWorkbook {
    private val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val assetHeaders = listOf("category", "name", "value", "date")
    private val budgetBaseHeaders = listOf("day", "type", "category", "title", "amount", "memo")
    private val budgetHeaders = budgetBaseHeaders + listOf("include_in_assets", "include_in_future_assets")

    fun encode(assets: List<AssetRecord>, budget: List<ExpenseEntry>): ByteArray {
        val assetRows = listOf(assetHeaders) + assets.map { record ->
            listOf(
                record.category.orEmpty(),
                record.name.orEmpty(),
                record.value?.toString().orEmpty(),
                record.date?.toInstant()?.atZone(ZoneId.systemDefault())?.format(dateFormatter).orEmpty(),
            )
        }
        val budgetRows = listOf(budgetHeaders) + budget.map { entry ->
            listOf(entry.day.toString(), entry.type, entry.category, entry.title, entry.amount.toString(), entry.memo, entry.includeInAssets.toString(), entry.includeInFutureAssets.toString())
        }
        return ByteArrayOutputStream().use { bytes ->
            ZipOutputStream(bytes).use { zip ->
                zip.addText("[Content_Types].xml", contentTypes)
                zip.addText("_rels/.rels", rootRelationships)
                zip.addText("xl/workbook.xml", workbook)
                zip.addText("xl/_rels/workbook.xml.rels", workbookRelationships)
                zip.addText("xl/worksheets/sheet1.xml", sheetXml(assetRows, listOf(false, false, true, false)))
                zip.addText("xl/worksheets/sheet2.xml", sheetXml(budgetRows, listOf(true, false, false, false, true, false, false, false)))
            }
            bytes.toByteArray()
        }
    }

    fun decode(input: InputStream): WorkbookImport {
        val files = ZipInputStream(input).use { zip ->
            buildMap {
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (!entry.isDirectory) put(entry.name, zip.readBytes())
                    zip.closeEntry()
                }
            }
        }
        require(files.containsKey("xl/workbook.xml")) { "올바른 Excel 통합 문서가 아닙니다." }
        val sharedStrings = files["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
        val workbookDoc = parseXml(files.getValue("xl/workbook.xml"))
        val relsDoc = parseXml(files.getValue("xl/_rels/workbook.xml.rels"))
        val targets = relsDoc.getElementsByTagName("Relationship").let { nodes ->
            (0 until nodes.length).associate { index ->
                val node = nodes.item(index)
                node.attributes.getNamedItem("Id").nodeValue to node.attributes.getNamedItem("Target").nodeValue
            }
        }
        val sheets = workbookDoc.getElementsByTagName("sheet").let { nodes ->
            (0 until nodes.length).associate { index ->
                val node = nodes.item(index)
                val id = node.attributes.getNamedItemNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id")?.nodeValue
                    ?: node.attributes.getNamedItem("r:id")?.nodeValue
                    ?: error("워크시트 참조가 올바르지 않습니다.")
                node.attributes.getNamedItem("name").nodeValue to (targets[id] ?: error("워크시트를 찾을 수 없습니다."))
            }
        }
        fun rows(sheetName: String, columnCount: Int): List<List<String>> {
            val target = sheets[sheetName] ?: return emptyList()
            val path = if (target.startsWith("/")) target.removePrefix("/") else "xl/${target.removePrefix("xl/")}"
            val content = files[path] ?: error("'$sheetName' 시트를 읽을 수 없습니다.")
            return parseSheet(content, sharedStrings, columnCount)
        }
        return WorkbookImport(
            assets = parseAssets(rows("자산 내역", assetHeaders.size)),
            budget = parseBudget(rows("가계부", budgetHeaders.size)),
        )
    }

    private fun parseAssets(rows: List<List<String>>): List<AssetRecord> {
        if (rows.isEmpty()) return emptyList()
        require(rows.first().take(assetHeaders.size) == assetHeaders) { "'자산 내역' 시트의 열 이름을 확인해 주세요." }
        return rows.drop(1).filter { it.any(String::isNotEmpty) }.mapIndexed { index, row ->
            require(row.size >= assetHeaders.size) { "자산 내역 ${index + 2}행의 열이 부족합니다." }
            val value = row[2].takeIf(String::isNotBlank)?.toIntOrNull()
                ?: if (row[2].isBlank()) null else error("자산 내역 ${index + 2}행의 금액이 숫자가 아닙니다.")
            val date = row[3].takeIf(String::isNotBlank)?.let { text ->
                runCatching { Date.from(OffsetDateTime.parse(text, dateFormatter).toInstant()) }
                    .getOrElse { error("자산 내역 ${index + 2}행의 날짜 형식이 올바르지 않습니다.") }
            }
            AssetRecord(category = row[0].ifBlank { null }, name = row[1].ifBlank { null }, value = value, date = date)
        }
    }

    private fun parseBudget(rows: List<List<String>>): List<ExpenseEntry> {
        if (rows.isEmpty()) return emptyList()
        val hasAssetFlag = rows.first().getOrNull(6) == "include_in_assets"
        val hasFutureFlag = rows.first().getOrNull(7) == "include_in_future_assets"
        require(rows.first().take(budgetBaseHeaders.size) == budgetBaseHeaders &&
            (rows.first().getOrNull(6).isNullOrEmpty() || hasAssetFlag) &&
            (rows.first().getOrNull(7).isNullOrEmpty() || hasFutureFlag) &&
            (!hasFutureFlag || hasAssetFlag)) { "'가계부' 시트의 열 이름을 확인해 주세요." }
        return rows.drop(1).filter { it.any(String::isNotEmpty) }.mapIndexed { index, row ->
            require(row.size >= budgetBaseHeaders.size) { "가계부 ${index + 2}행의 열이 부족합니다." }
            val day = row[0].toIntOrNull() ?: error("가계부 ${index + 2}행의 날짜가 올바르지 않습니다.")
            val type = row[1]
            require(type in setOf("수입", "지출", "저축")) { "가계부 ${index + 2}행의 유형은 수입, 지출, 저축 중 하나여야 합니다." }
            val amount = row[4].toLongOrNull() ?: error("가계부 ${index + 2}행의 금액이 숫자가 아닙니다.")
            ExpenseEntry(day = day, type = type, category = row[2], title = row[3], amount = amount, memo = row[5], includeInAssets = hasAssetFlag && row.getOrNull(6).equals("true", ignoreCase = true), includeInFutureAssets = hasFutureFlag && row.getOrNull(7).equals("true", ignoreCase = true))
        }
    }

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        val doc = parseXml(bytes)
        val items = doc.getElementsByTagName("si")
        return (0 until items.length).map { index ->
            val texts = (items.item(index) as org.w3c.dom.Element).getElementsByTagName("t")
            (0 until texts.length).joinToString("") { texts.item(it).textContent }
        }
    }

    private fun parseSheet(bytes: ByteArray, sharedStrings: List<String>, columnCount: Int): List<List<String>> {
        val doc = parseXml(bytes)
        val rowNodes = doc.getElementsByTagName("row")
        return (0 until rowNodes.length).map { rowIndex ->
            val cells = (rowNodes.item(rowIndex) as org.w3c.dom.Element).getElementsByTagName("c")
            val values = mutableMapOf<Int, String>()
            (0 until cells.length).forEach { cellIndex ->
                val cell = cells.item(cellIndex) as org.w3c.dom.Element
                val ref = cell.getAttribute("r")
                val column = columnIndex(ref)
                val type = cell.getAttribute("t")
                val value = when (type) {
                    "inlineStr" -> cell.getElementsByTagName("t").item(0)?.textContent.orEmpty()
                    "s" -> cell.getElementsByTagName("v").item(0)?.textContent?.toIntOrNull()?.let(sharedStrings::getOrNull).orEmpty()
                    else -> cell.getElementsByTagName("v").item(0)?.textContent.orEmpty()
                }
                values[column] = value
            }
            (0 until columnCount).map { values[it].orEmpty() }
        }
    }

    private fun columnIndex(reference: String): Int {
        val letters = reference.takeWhile(Char::isLetter)
        require(letters.isNotEmpty()) { "Excel 셀 주소가 올바르지 않습니다." }
        return letters.fold(0) { value, char -> value * 26 + (char.uppercaseChar() - 'A' + 1) } - 1
    }

    private fun parseXml(bytes: ByteArray): org.w3c.dom.Document {
        val xmlText = bytes.toString(Charsets.UTF_8)
        require(!xmlText.contains("<!DOCTYPE", ignoreCase = true)) { "Excel XML에 허용되지 않는 DTD 선언이 있습니다." }
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isExpandEntityReferences = false
        }
        return factory.newDocumentBuilder().parse(ByteArrayInputStream(bytes))
    }

    private fun sheetXml(rows: List<List<String>>, numericColumns: List<Boolean>): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>")
        rows.forEachIndexed { rowIndex, row ->
            append("<row r=\"${rowIndex + 1}\">")
            row.forEachIndexed { columnIndex, value ->
                if (value.isNotEmpty()) {
                    val ref = columnName(columnIndex) + (rowIndex + 1)
                    if (rowIndex > 0 && numericColumns.getOrElse(columnIndex) { false }) {
                        append("<c r=\"$ref\"><v>${xmlEscape(value)}</v></c>")
                    } else {
                        append("<c r=\"$ref\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${xmlEscape(value)}</t></is></c>")
                    }
                }
            }
            append("</row>")
        }
        append("</sheetData></worksheet>")
    }

    private fun columnName(index: Int): String {
        var value = index + 1
        return buildString {
            while (value > 0) {
                val remainder = (value - 1) % 26
                insert(0, ('A'.code + remainder).toChar())
                value = (value - 1) / 26
            }
        }
    }

    private fun xmlEscape(value: String): String = value
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;")

    private fun ZipOutputStream.addText(path: String, content: String) {
        putNextEntry(ZipEntry(path))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>"""
    private val rootRelationships = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""
    private val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="자산 내역" sheetId="1" r:id="rId1"/><sheet name="가계부" sheetId="2" r:id="rId2"/></sheets></workbook>"""
    private val workbookRelationships = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/></Relationships>"""
}
