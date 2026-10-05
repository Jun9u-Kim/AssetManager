package com.sample.financemanager

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sample.financemanager.data.FinanceRecord
import com.sample.financemanager.ui.theme.FinanceManagerTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceManagerTheme {
                FinanceManagerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceManagerApp(viewModel: MainViewModel = viewModel()) {
    val records by viewModel.records.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<FinanceRecord?>(null) }
    var assetToEdit by remember { mutableStateOf<FinanceRecord?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "자산 관리",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "자산 내역 추가",
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (records.isEmpty()) {
                EmptyStateView()
            } else {
                FinanceRecordList(
                    records = records,
                    onEdit = { record -> assetToEdit = record },
                    onDelete = { record -> recordToDelete = record },
                )
            }

            // 1. Add New Record Dialog
            if (showAddDialog) {
                AddRecordDialog(
                    existingRecords = records,
                    onDismiss = { showAddDialog = false },
                    onAddRecord = { category, name, value, date ->
                        viewModel.addRecord(category, name, value, date)
                        showAddDialog = false
                    },
                )
            }

            // 2. Delete Confirmation Dialog
            recordToDelete?.let { record ->
                DeleteConfirmationDialog(
                    record = record,
                    onDismiss = { recordToDelete = null },
                    onConfirmDelete = {
                        viewModel.deleteRecord(record)
                        recordToDelete = null
                    },
                )
            }

            // 3. Edit Asset History Dialog
            assetToEdit?.let { record ->
                EditAssetHistoryDialog(
                    asset = record,
                    viewModel = viewModel,
                    onDismiss = { assetToEdit = null },
                )
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "등록된 자산 내역이 없습니다.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "오른쪽 아래 '+' 버튼을 눌러\n새로운 자산 내역을 추가해보세요.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun FinanceRecordList(
    records: List<FinanceRecord>,
    onEdit: (FinanceRecord) -> Unit,
    onDelete: (FinanceRecord) -> Unit,
) {
    val totalValue = remember(records) {
        records.sumOf { it.value ?: 0 }
    }
    val currencyFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "총 자산",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "${currencyFormat.format(totalValue)} 원",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(records, key = { it.uid }) { record ->
                FinanceRecordCard(
                    record = record,
                    currencyFormat = currencyFormat,
                    dateFormat = dateFormat,
                    onEdit = { onEdit(record) },
                    onDelete = { onDelete(record) },
                )
            }
        }
    }
}

@Composable
fun FinanceRecordCard(
    record: FinanceRecord,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Line 1 (Top): Category & Name on left, Edit & Delete buttons on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryChip(category = record.category ?: "기타")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.name ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "편집",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Line 2 (Middle): Value Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "자산 금액",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = "${currencyFormat.format(record.value ?: 0)} 원",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Line 3 (Bottom): Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "기준일",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = record.date?.let { dateFormat.format(it) } ?: "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun CategoryChip(category: String) {
    val (backgroundColor, textColor) = when (category) {
        "부동산" -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        "금" -> Pair(Color(0xFFFFF8E1), Color(0xFFF57F17))
        "자동차" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "입출금" -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        else -> Pair(Color(0xFFECEFF1), Color(0xFF455A64))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}

// Delete Confirmation Dialog
@Composable
fun DeleteConfirmationDialog(
    record: FinanceRecord,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "자산 내역 삭제",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "[${record.category}] ${record.name} 내역을 정말 삭제하시겠습니까?",
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

// Edit Asset History Dialog
@Composable
fun EditAssetHistoryDialog(
    asset: FinanceRecord,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    val category = asset.category ?: ""
    val name = asset.name ?: ""

    val historyRecords by viewModel.getHistoryForAsset(category, name)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddHistoryDialog by remember { mutableStateOf(false) }
    var recordToDeleteInHistory by remember { mutableStateOf<FinanceRecord?>(null) }

    val currencyFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = "자산 이력 편집",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryChip(category = category)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "히스토리 (총 ${historyRecords.size}건)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    OutlinedButton(
                        onClick = { showAddHistoryDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "이전 날짜 추가",
                            modifier = Modifier.height(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "날짜 내역 추가", fontSize = 12.sp)
                    }
                }

                if (historyRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "히스토리 내역이 없습니다.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(historyRecords, key = { it.uid }) { historyItem ->
                            HistoryRecordRow(
                                record = historyItem,
                                currencyFormat = currencyFormat,
                                dateFormat = dateFormat,
                                onUpdateRecord = { updatedRecord ->
                                    viewModel.updateRecord(updatedRecord)
                                },
                                onDeleteRecord = {
                                    recordToDeleteInHistory = historyItem
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("닫기")
            }
        },
    )

    // Sub-dialog to Add New History Entry for this Asset
    if (showAddHistoryDialog) {
        AddHistoryRecordDialog(
            category = category,
            name = name,
            onDismiss = { showAddHistoryDialog = false },
            onAddRecord = { date, value ->
                viewModel.addRecord(category, name, value, date)
                showAddHistoryDialog = false
            },
        )
    }

    // Delete sub-confirmation inside history editor
    recordToDeleteInHistory?.let { record ->
        DeleteConfirmationDialog(
            record = record,
            onDismiss = { recordToDeleteInHistory = null },
            onConfirmDelete = {
                viewModel.deleteRecord(record)
                recordToDeleteInHistory = null
            },
        )
    }
}

@Composable
fun HistoryRecordRow(
    record: FinanceRecord,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onUpdateRecord: (FinanceRecord) -> Unit,
    onDeleteRecord: () -> Unit,
) {
    val context = LocalContext.current

    var valueText by remember(record.value) { mutableStateOf((record.value ?: 0).toString()) }
    var currentDate by remember(record.date) { mutableStateOf(record.date ?: Date()) }
    var isEditingValue by remember { mutableStateOf(false) }

    // DatePicker for row
    val calendar = remember(currentDate) { Calendar.getInstance().apply { time = currentDate } }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val newDate = newCal.time
                currentDate = newDate
                onUpdateRecord(record.copy(date = newDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Line 1: Date Chip on left, Action buttons on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Date Picker Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { datePickerDialog.show() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "날짜 변경",
                            modifier = Modifier.height(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateFormat.format(currentDate),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                // Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditingValue) {
                        IconButton(
                            onClick = {
                                val newVal = valueText.toIntOrNull() ?: record.value ?: 0
                                onUpdateRecord(record.copy(value = newVal))
                                isEditingValue = false
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "저장",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        IconButton(onClick = { isEditingValue = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "금액 수정",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.height(20.dp),
                            )
                        }
                    }
                    IconButton(onClick = onDeleteRecord) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "이력 삭제",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.height(20.dp),
                        )
                    }
                }
            }

            // Line 2: Amount (Full-width Input or Text)
            if (isEditingValue) {
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { input -> valueText = input.filter { it.isDigit() } },
                    label = { Text("금액 (원)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingValue = true }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "자산 금액",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "${currencyFormat.format(record.value ?: 0)} 원",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
fun AddHistoryRecordDialog(
    category: String,
    name: String,
    onDismiss: () -> Unit,
    onAddRecord: (date: Date, value: Int) -> Unit,
) {
    val context = LocalContext.current

    var valueText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }

    val dateFormat = remember { SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA) }

    val calendar = remember(selectedDate) { Calendar.getInstance().apply { time = selectedDate } }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDate = newCal.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "이전 날짜 내역 추가",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "자산: [$category] $name",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Date Selection
                Box {
                    OutlinedTextField(
                        value = dateFormat.format(selectedDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("날짜 선택") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "달력에서 선택",
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { datePickerDialog.show() },
                            ),
                    )
                }

                // Value Input
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { input -> valueText = input.filter { it.isDigit() } },
                    label = { Text("금액 (원)") },
                    placeholder = { Text("예: 1200000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valueInt = valueText.toIntOrNull() ?: 0
                    onAddRecord(selectedDate, valueInt)
                },
                enabled = valueText.isNotBlank(),
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    existingRecords: List<FinanceRecord>,
    onDismiss: () -> Unit,
    onAddRecord: (category: String, name: String, value: Int, date: Date) -> Unit,
) {
    val context = LocalContext.current

    // Category options setup
    val defaultCategories = remember { listOf("부동산", "금", "자동차", "입출금", "기타") }
    val existingCategories = remember(existingRecords) {
        existingRecords.mapNotNull { it.category }.filter { it.isNotBlank() }.distinct()
    }
    val categoryList = remember(existingCategories) {
        (defaultCategories + existingCategories).distinct().toMutableStateList()
    }

    var selectedCategory by remember { mutableStateOf(categoryList.firstOrNull() ?: "기타") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }

    // Preset Name suggestions per category
    val defaultNameMap = remember {
        mapOf(
            "부동산" to listOf("아파트", "상가", "토지", "오피스텔"),
            "금" to listOf("골드바", "금반지", "금괴"),
            "자동차" to listOf("승용차", "전기차", "오토바이"),
            "입출금" to listOf("예금", "적금", "파킹통장", "CMA"),
            "기타" to listOf("기타 자산"),
        )
    }

    // Dynamic Name list for selected category
    val initialNamesForCategory = remember(selectedCategory, existingRecords) {
        val defaults = defaultNameMap[selectedCategory] ?: emptyList()
        val fromDb = existingRecords
            .filter { it.category == selectedCategory }
            .mapNotNull { it.name }
            .filter { it.isNotBlank() }
        (defaults + fromDb).distinct()
    }

    val nameList = remember(selectedCategory) {
        if (initialNamesForCategory.isEmpty()) {
            mutableStateListOf("자산")
        } else {
            initialNamesForCategory.toMutableStateList()
        }
    }

    var selectedName by remember(selectedCategory) {
        mutableStateOf(nameList.firstOrNull() ?: "")
    }
    var nameDropdownExpanded by remember { mutableStateOf(false) }
    var showAddNameDialog by remember { mutableStateOf(false) }
    var customNameText by remember { mutableStateOf("") }

    var valueText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }

    val dateFormat = remember { SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA) }

    // DatePickerDialog handler
    val calendar = remember(selectedDate) {
        Calendar.getInstance().apply { time = selectedDate }
    }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDate = newCal.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    val isInputValid = selectedCategory.isNotBlank() && selectedName.isNotBlank() && valueText.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "자산 내역 추가",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 1. Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("카테고리") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                    ) {
                        categoryList.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                },
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ 새 카테고리 추가...",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            onClick = {
                                categoryDropdownExpanded = false
                                customCategoryText = ""
                                showAddCategoryDialog = true
                            },
                        )
                    }
                }

                // 2. Name Dropdown
                ExposedDropdownMenuBox(
                    expanded = nameDropdownExpanded,
                    onExpandedChange = { nameDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("이름") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nameDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = nameDropdownExpanded,
                        onDismissRequest = { nameDropdownExpanded = false },
                    ) {
                        nameList.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedName = name
                                    nameDropdownExpanded = false
                                },
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ 새 이름 추가...",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            onClick = {
                                nameDropdownExpanded = false
                                customNameText = ""
                                showAddNameDialog = true
                            },
                        )
                    }
                }

                // 3. Value Input
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { input ->
                        valueText = input.filter { it.isDigit() }
                    },
                    label = { Text("금액 (원)") },
                    placeholder = { Text("예: 1000000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 4. Date Selection (Calendar Picker)
                Box {
                    OutlinedTextField(
                        value = dateFormat.format(selectedDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("날짜") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "달력에서 선택",
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // Transparent overlay to catch click for DatePicker
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { datePickerDialog.show() },
                            ),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valueInt = valueText.toIntOrNull() ?: 0
                    onAddRecord(selectedCategory, selectedName, valueInt, selectedDate)
                },
                enabled = isInputValid,
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )

    // Sub-dialog to add a custom Category
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("새 카테고리 추가") },
            text = {
                OutlinedTextField(
                    value = customCategoryText,
                    onValueChange = { customCategoryText = it },
                    label = { Text("카테고리명") },
                    placeholder = { Text("예: 주식, 채권, 암호화폐") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customCategoryText.trim()
                        if (trimmed.isNotBlank()) {
                            if (!categoryList.contains(trimmed)) {
                                categoryList.add(trimmed)
                            }
                            selectedCategory = trimmed
                        }
                        showAddCategoryDialog = false
                    },
                    enabled = customCategoryText.isNotBlank(),
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddCategoryDialog = false }) {
                    Text("취소")
                }
            },
        )
    }

    // Sub-dialog to add a custom Name
    if (showAddNameDialog) {
        AlertDialog(
            onDismissRequest = { showAddNameDialog = false },
            title = { Text("새 이름 추가") },
            text = {
                OutlinedTextField(
                    value = customNameText,
                    onValueChange = { customNameText = it },
                    label = { Text("자산 이름") },
                    placeholder = { Text("예: 삼성전자, 카카오뱅크") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customNameText.trim()
                        if (trimmed.isNotBlank()) {
                            if (!nameList.contains(trimmed)) {
                                nameList.add(trimmed)
                            }
                            selectedName = trimmed
                        }
                        showAddNameDialog = false
                    },
                    enabled = customNameText.isNotBlank(),
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddNameDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}
