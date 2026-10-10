package com.lgtm.asset_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lgtm.asset_manager.data.ExpenseEntry
import com.lgtm.asset_manager.data.AssetRecord
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private val entryTypes = listOf("수입", "지출", "저축")
private val incomeColor = Color(0xFFD93636)
private val expenseColor = Color(0xFF1976D2)
private val savingsColor = Color(0xFF2E7D32)

@Composable
fun BudgetScreen(
    entries: List<ExpenseEntry>,
    assetRecords: List<AssetRecord>,
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
    onAdd: (ExpenseEntry) -> Unit,
    onUpdate: (ExpenseEntry) -> Unit,
    onDelete: (ExpenseEntry) -> Unit,
) {
    val today = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(dayKey(today)) }
    var entryToEdit by remember { mutableStateOf<ExpenseEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<ExpenseEntry?>(null) }
    fun moveMonth(delta: Int) {
        val target = Calendar.getInstance().apply { set(year, month, 1); add(Calendar.MONTH, delta) }
        year = target.get(Calendar.YEAR)
        month = target.get(Calendar.MONTH)
        selectedDay = year * 10000 + (month + 1) * 100 + 1
    }
    val monthCalendar = remember(year, month) {
        Calendar.getInstance().apply { set(year, month, 1); set(Calendar.DAY_OF_MONTH, 1) }
    }
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val leadingDays = (monthCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
    val monthEntries = remember(entries, year, month) { entries.filter { it.day / 100 == year * 100 + month + 1 } }
    val selectedEntries = remember(entries, selectedDay) { entries.filter { it.day == selectedDay } }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    val monthlyIncome = monthEntries.filter { it.type == "수입" }.sumOf { it.amount }
    val monthlyExpense = monthEntries.filter { it.type == "지출" }.sumOf { it.amount }
    val monthlySavings = monthEntries.filter { it.type == "저축" }.sumOf { it.amount }
    val monthlyBalance = monthlyIncome - monthlyExpense

    val cellCount = ((leadingDays + daysInMonth + 6) / 7) * 7
    val cellHeight = 72.dp
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 88.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = { moveMonth(-1) }) { Icon(Icons.Default.ChevronLeft, "이전 달") }
            Text("${year}년 ${month + 1}월", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { moveMonth(1) }) { Icon(Icons.Default.ChevronRight, "다음 달") }
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp)) {
                SummaryAmount("수입", monthlyIncome, incomeColor, numberFormat, Modifier.weight(1f))
                SummaryAmount("지출", monthlyExpense, expenseColor, numberFormat, Modifier.weight(1f))
                SummaryAmount("합계", monthlyBalance, MaterialTheme.colorScheme.onSurface, numberFormat, Modifier.weight(1f))
                SummaryAmount("저축", monthlySavings, savingsColor, numberFormat, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { index, label ->
                Text(label, Modifier.weight(1f).padding(vertical = 4.dp), textAlign = TextAlign.Center,
                    color = when (index) { 0 -> incomeColor; 6 -> expenseColor; else -> MaterialTheme.colorScheme.onSurfaceVariant },
                    fontWeight = FontWeight.SemiBold)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("+ 남은 금액", fontSize = 9.sp, color = incomeColor)
            Spacer(Modifier.width(14.dp))
            Text("− 초과 지출", fontSize = 9.sp, color = expenseColor)
            Spacer(Modifier.width(14.dp))
            Text("저축", fontSize = 9.sp, color = savingsColor)
        }
        repeat(cellCount / 7) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { weekday ->
                    val day = week * 7 + weekday - leadingDays + 1
                    if (day in 1..daysInMonth) {
                        val key = year * 10000 + (month + 1) * 100 + day
                        val dayEntries = monthEntries.filter { it.day == key }
                        val income = dayEntries.filter { it.type == "수입" }.sumOf { it.amount }
                        val expense = dayEntries.filter { it.type == "지출" }.sumOf { it.amount }
                        val savings = dayEntries.filter { it.type == "저축" }.sumOf { it.amount }
                        val isSelected = selectedDay == key
                        Column(
                            Modifier.weight(1f).height(cellHeight).padding(1.dp)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f), RoundedCornerShape(10.dp))
                                .clickable { selectedDay = key }.padding(horizontal = 1.dp, vertical = 1.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("$day", fontSize = 12.sp, lineHeight = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when (weekday) { 0 -> incomeColor; 6 -> expenseColor; else -> MaterialTheme.colorScheme.onSurface })
                            if (income > 0 || expense > 0) {
                                val difference = income - expense
                                val absoluteDifference = kotlin.math.abs(difference)
                                Text(
                                    text = buildAnnotatedString {
                                        val color = when {
                                            difference > 0 -> incomeColor
                                            difference < 0 -> expenseColor
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                        withStyle(SpanStyle(color = color)) {
                                            append(
                                                when {
                                                    difference > 0 -> "+"
                                                    difference < 0 -> "−"
                                                    else -> ""
                                                },
                                            )
                                            append(compactAmount(absoluteDifference))
                                            if (absoluteDifference < 10_000) append("원")
                                        }
                                    },
                                    fontSize = 8.sp,
                                    lineHeight = 10.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            if (savings > 0) Text("저축 ${compactAmount(savings)}${if (savings < 10_000) "원" else ""}", fontSize = 8.sp, lineHeight = 10.sp, color = savingsColor, maxLines = 1, softWrap = false)
                        }
                    } else Spacer(Modifier.weight(1f).height(cellHeight))
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 5.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            val displayDate = selectedDay.toString()
            Text("${displayDate.substring(0, 4)}년 ${displayDate.substring(4, 6)}월 ${displayDate.substring(6, 8)}일 · ${selectedEntries.size}건", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Start)
        }
        val incomeTotal = selectedEntries.filter { it.type == "수입" }.sumOf { it.amount }
        val expenseTotal = selectedEntries.filter { it.type == "지출" }.sumOf { it.amount }
        val savingsTotal = selectedEntries.filter { it.type == "저축" }.sumOf { it.amount }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            Text("수입 ${compactAmount(incomeTotal)}원", style = MaterialTheme.typography.labelSmall, color = incomeColor)
            Text("지출 ${compactAmount(expenseTotal)}원", style = MaterialTheme.typography.labelSmall, color = expenseColor)
            Text("저축 ${compactAmount(savingsTotal)}원", style = MaterialTheme.typography.labelSmall, color = savingsColor)
        }
        if (selectedEntries.isEmpty()) {
            Text("등록된 내역이 없습니다.", modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                selectedEntries.forEachIndexed { index, entry ->
                    val entryColor = when (entry.type) {
                        "수입" -> incomeColor
                        "지출" -> expenseColor
                        else -> savingsColor
                    }
                    val signedAmount = when (entry.type) {
                        "수입" -> "+${numberFormat.format(entry.amount)}원"
                        "지출" -> "−${numberFormat.format(entry.amount)}원"
                        else -> "${numberFormat.format(entry.amount)}원"
                    }
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Surface(shape = RoundedCornerShape(50), color = entryColor.copy(alpha = 0.12f)) {
                                        Row(
                                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Icon(Icons.Default.Category, contentDescription = null, tint = entryColor, modifier = Modifier.size(12.dp))
                                            Text("${entry.type} · ${entry.category}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = entryColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    Text(entry.title, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Text(signedAmount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = entryColor)
                                    if (entry.memo.isNotBlank()) {
                                        Text("·", color = MaterialTheme.colorScheme.outline)
                                        Text(entry.memo, modifier = Modifier.weight(1f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                            IconButton(onClick = { entryToEdit = entry }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "편집", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { entryToDelete = entry }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "삭제", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (index < selectedEntries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    }
                }
            }
        }
    }
    if (showAddDialog) BudgetEntryDialog(selectedDay, entries, assetRecords, null, onDismiss = onDismissAddDialog, onSave = { entry -> onAdd(entry); onDismissAddDialog() })
    entryToEdit?.let { entry ->
        BudgetEntryDialog(entry.day, entries, assetRecords, entry, onDismiss = { entryToEdit = null }, onSave = { updated -> onUpdate(updated); entryToEdit = null })
    }
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("가계부 항목 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("[${entry.category}] ${entry.title} · ${NumberFormat.getNumberInstance(Locale.KOREA).format(entry.amount)}원을 삭제할까요?") },
            confirmButton = {
                TextButton(onClick = { onDelete(entry); entryToDelete = null }) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { entryToDelete = null }) { Text("취소") } },
        )
    }
}

@Composable
private fun SummaryAmount(label: String, amount: Long, color: Color, numberFormat: NumberFormat, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(numberFormat.format(amount), style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
        Text("원", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetEntryDialog(day: Int, entries: List<ExpenseEntry>, assetRecords: List<AssetRecord>, initialEntry: ExpenseEntry?, onDismiss: () -> Unit, onSave: (ExpenseEntry) -> Unit) {
    var type by remember(initialEntry?.id) { mutableStateOf(initialEntry?.type ?: "지출") }
    val defaultCategories = remember {
        mapOf(
            "수입" to listOf("급여", "용돈", "이자", "기타"),
            "지출" to listOf("식비", "교통", "주거/공과금", "쇼핑", "의료", "문화/여가", "기타"),
            "저축" to listOf("예금/적금", "투자", "비상금", "기타"),
        )
    }
    val homeCategories = remember(assetRecords) {
        (listOf("부동산", "금", "자동차", "입출금", "기타") + assetRecords.mapNotNull { it.category }.filter { it.isNotBlank() }).distinct()
    }
    val categoryOptions = remember(entries, assetRecords, type, initialEntry?.category) {
        if (type == "저축") {
            (homeCategories + listOfNotNull(initialEntry?.category?.takeIf { it.isNotBlank() })).distinct()
        } else {
            (defaultCategories.getValue(type) + entries.filter { it.type == type }.map { it.category }.filter { it.isNotBlank() } + listOfNotNull(initialEntry?.category?.takeIf { it.isNotBlank() })).distinct()
        }
    }
    var category by remember(type, initialEntry?.id) { mutableStateOf(initialEntry?.category?.takeIf { it.isNotBlank() } ?: categoryOptions.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }
    val defaultTitles = remember {
        mapOf(
            "수입" to listOf("급여", "용돈", "이자"),
            "지출" to listOf("식사", "교통", "공과금", "쇼핑", "병원", "문화생활"),
            "저축" to emptyList(),
        )
    }
    val titleOptions = remember(entries, assetRecords, type, category, initialEntry?.title) {
        val recordedNames = if (type == "저축") {
            assetRecords.filter { it.category == category }.mapNotNull { it.name } +
                entries.filter { it.type == type && it.category == category }.map { it.title }
        } else {
            entries.filter { it.type == type && it.category == category }.map { it.title }
        }
        (defaultTitles.getValue(type) + recordedNames + listOfNotNull(initialEntry?.title?.takeIf { it.isNotBlank() })).filter { it.isNotBlank() }.distinct()
    }
    var title by remember(type, category, initialEntry?.id) { mutableStateOf(initialEntry?.title ?: titleOptions.firstOrNull().orEmpty()) }
    var titleExpanded by remember { mutableStateOf(false) }
    var showAddTitle by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var amountText by remember(initialEntry?.id) { mutableStateOf(initialEntry?.amount?.toString() ?: "") }
    var memo by remember(initialEntry?.id) { mutableStateOf(initialEntry?.memo ?: "") }
    var includeInAssets by remember(initialEntry?.id) { mutableStateOf(initialEntry?.includeInAssets ?: false) }
    var includeInFutureAssets by remember(initialEntry?.id) { mutableStateOf(initialEntry?.includeInFutureAssets ?: false) }
    val validAmount = amountText.toLongOrNull()?.takeIf { it > 0 }
    val date = day.toString()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "${date.substring(4, 6)}월 ${date.substring(6, 8)}일 내역",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (initialEntry == null) "새 항목 추가" else "항목 편집",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entryTypes.forEach { option -> FilterChip(selected = type == option, onClick = { type = option; if (option != "저축") { includeInAssets = false; includeInFutureAssets = false } }, label = { Text(option) }) }
                }
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("카테고리") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categoryOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { category = option; categoryExpanded = false }) }
                        if (type != "저축") {
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("+ 새 카테고리 추가...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }, onClick = { categoryExpanded = false; newCategory = ""; showAddCategory = true })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = titleExpanded, onExpandedChange = { titleExpanded = it }) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("항목명") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = titleExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = titleExpanded, onDismissRequest = { titleExpanded = false }) {
                        titleOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { title = option; titleExpanded = false }) }
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("+ 새 항목명 추가...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }, onClick = { titleExpanded = false; newTitle = ""; showAddTitle = true })
                    }
                }
                OutlinedTextField(amountText, { amountText = it.filter(Char::isDigit) }, label = { Text("금액") }, suffix = { Text("원") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(memo, { memo = it }, label = { Text("메모 (선택)") }, singleLine = true)
                if (type == "저축") {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = includeInAssets, onCheckedChange = { includeInAssets = it; if (!it) includeInFutureAssets = false })
                            Text("자산에도 저축 금액 더하기", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = includeInFutureAssets, enabled = includeInAssets, onCheckedChange = { includeInFutureAssets = it })
                            Text("이후 자산 기록에도 금액 더하기", style = MaterialTheme.typography.bodyMedium, color = if (includeInAssets) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(enabled = category.isNotBlank() && title.isNotBlank() && validAmount != null, onClick = { validAmount?.let { onSave(ExpenseEntry(id = initialEntry?.id ?: 0, day = day, type = type, category = category, title = title.trim(), amount = it, memo = memo.trim(), includeInAssets = type == "저축" && includeInAssets, includeInFutureAssets = type == "저축" && includeInAssets && includeInFutureAssets)) } }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text("새 카테고리 추가") },
            text = { OutlinedTextField(newCategory, { newCategory = it }, label = { Text("카테고리명") }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = newCategory.isNotBlank(), onClick = {
                    val trimmed = newCategory.trim()
                    if (trimmed.isNotBlank()) category = trimmed
                    showAddCategory = false
                }) { Text("추가") }
            },
            dismissButton = { TextButton(onClick = { showAddCategory = false }) { Text("취소") } },
        )
    }
    if (showAddTitle) {
        AlertDialog(
            onDismissRequest = { showAddTitle = false },
            title = { Text("새 항목명 추가") },
            text = { OutlinedTextField(newTitle, { newTitle = it }, label = { Text("항목명") }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = newTitle.isNotBlank(), onClick = {
                    val trimmed = newTitle.trim()
                    if (trimmed.isNotBlank()) title = trimmed
                    showAddTitle = false
                }) { Text("추가") }
            },
            dismissButton = { TextButton(onClick = { showAddTitle = false }) { Text("취소") } },
        )
    }
}

private fun dayKey(calendar: Calendar): Int = calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH)

private fun compactAmount(amount: Long): String = when {
    amount >= 100_000_000 -> "${amount / 100_000_000}억"
    amount >= 10_000 -> "${amount / 10_000}만"
    else -> amount.toString()
}
