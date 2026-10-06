package com.lgtm.assetmanager.data

import java.io.InputStream
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

/** CSV format used for lossless backup and restore of asset history. */
object AssetCsv {
    private val headers = listOf("category", "name", "value", "date")
    private val legacyHeaders = listOf("category", "name", "value", "date_epoch_ms")
    private val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun encode(records: List<AssetRecord>): String = buildString {
        append('\uFEFF')
        appendLine(headers.joinToString(","))
        records.forEach { record ->
            appendLine(
                listOf(
                    record.category.orEmpty(),
                    record.name.orEmpty(),
                    record.value?.toString().orEmpty(),
                    record.date?.toInstant()
                        ?.atZone(ZoneId.systemDefault())
                        ?.format(dateFormatter)
                        .orEmpty(),
                ).joinToString(",") { it.toCsvField() },
            )
        }
    }

    fun decode(input: InputStream): List<AssetRecord> {
        val text = input.bufferedReader(Charsets.UTF_8).use { it.readText() }.removePrefix("\uFEFF")
        val rows = parseRows(text).filter { row -> row.any(String::isNotEmpty) }
        require(rows.isNotEmpty()) { "CSV 파일에 열 이름이 없습니다." }
        val inputHeaders = rows.first().map(String::trim)
        val isLegacyFormat = inputHeaders == legacyHeaders
        require(inputHeaders == headers || isLegacyFormat) {
            "CSV 열 이름이 올바르지 않습니다. category,name,value,date 순서가 필요합니다."
        }

        return rows.drop(1).mapIndexed { index, row ->
            require(row.size == headers.size) { "${index + 2}번째 행의 열 개수가 올바르지 않습니다." }
            val value = row[2].takeIf(String::isNotEmpty)?.toIntOrNull()
                ?: if (row[2].isEmpty()) null else throw IllegalArgumentException("${index + 2}번째 행의 value가 숫자가 아닙니다.")
            val date = row[3].takeIf(String::isNotEmpty)?.let { dateText ->
                if (isLegacyFormat) {
                    dateText.toLongOrNull()?.let(::Date)
                        ?: throw IllegalArgumentException("${index + 2}번째 행의 date_epoch_ms가 올바르지 않습니다.")
                } else {
                    runCatching {
                        Date.from(OffsetDateTime.parse(dateText, dateFormatter).toInstant())
                    }.getOrElse {
                        throw IllegalArgumentException("${index + 2}번째 행의 date는 ISO 8601 형식이어야 합니다.")
                    }
                }
            }

            AssetRecord(
                category = row[0].ifEmpty { null },
                name = row[1].ifEmpty { null },
                value = value,
                date = date,
            )
        }
    }

    private fun String.toCsvField(): String =
        if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${replace("\"", "\"\"")}\""
        } else {
            this
        }

    private fun parseRows(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var index = 0

        while (index < csv.length) {
            val char = csv[index]
            when {
                quoted && char == '"' && csv.getOrNull(index + 1) == '"' -> {
                    field.append('"')
                    index++
                }
                char == '"' -> quoted = !quoted
                char == ',' && !quoted -> {
                    row.add(field.toString())
                    field.setLength(0)
                }
                (char == '\n' || char == '\r') && !quoted -> {
                    row.add(field.toString())
                    field.setLength(0)
                    rows.add(row)
                    row = mutableListOf()
                    if (char == '\r' && csv.getOrNull(index + 1) == '\n') index++
                }
                else -> field.append(char)
            }
            index++
        }

        require(!quoted) { "CSV의 형식이 맞지 않습니다." }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }
        return rows
    }
}
