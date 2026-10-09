package com.lgtm.asset_manager

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.lgtm.asset_manager.data.AssetRecord
import com.lgtm.asset_manager.data.AssetCsv
import com.lgtm.asset_manager.ui.theme.Asset_ManagerTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val adsReady = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {
                runOnUiThread { adsReady.value = true }
            }
        }
        setContent {
            Asset_ManagerTheme {
                Asset_ManagerApp(showAds = adsReady.value)
            }
        }
    }

}

@Composable
private fun AdMobBanner() {
    val context = LocalContext.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        val widthDp = maxWidth.value.toInt()
        if (widthDp > 0) {
            val adSize = remember(widthDp) {
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
            }
            val adView = remember(widthDp) {
                AdView(context).apply {
                    adUnitId = if (BuildConfig.DEBUG) {
                        "ca-app-pub-3940256099942544/9214589741"
                    } else {
                        "ca-app-pub-3598334831467233/3045917343"
                    }
                    setAdSize(adSize)
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adSize.height.dp),
                factory = { view ->
                    adView.apply { loadAd(AdRequest.Builder().build()) }
                },
            )
            DisposableEffect(adView) {
                onDispose { adView.destroy() }
            }
        }
    }
}

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("홈", Icons.Filled.Home, Icons.Outlined.Home),
    STATS("통계/차트", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS("설정", Icons.Filled.Settings, Icons.Outlined.Settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Asset_ManagerApp(
    showAds: Boolean = true,
    viewModel: MainViewModel = viewModel(),
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pendingCsvImport by remember { mutableStateOf<List<AssetRecord>?>(null) }
    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use(AssetCsv::decode)
                            ?: error("선택한 파일을 열 수 없습니다.")
                    }
                }.onSuccess { imported ->
                    pendingCsvImport = imported
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        error.message ?: "CSV 파일을 읽지 못했습니다.",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    fun shareCsvFile() {
        coroutineScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val recordsToExport = viewModel.getAllRecordsSnapshot()
                    val exportDirectory = File(context.cacheDir, "exports").apply { mkdirs() }
                    val csvFile = File(exportDirectory, "asset_records_${System.currentTimeMillis()}.csv")
                    csvFile.writeText(AssetCsv.encode(recordsToExport), Charsets.UTF_8)
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        csvFile,
                    )
                }
            }.onSuccess { uri ->
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.ms-excel"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData(
                        "CSV 파일",
                        arrayOf("application/vnd.ms-excel"),
                        ClipData.Item(uri),
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(sendIntent, "CSV 파일 공유"))
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    error.message ?: "CSV 파일을 내보내지 못했습니다.",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    var currentTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    var showAddDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<AssetRecord?>(null) }
    var assetToEdit by remember { mutableStateOf<AssetRecord?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            MainTab.HOME -> "자산 관리"
                            MainTab.STATS -> "통계 및 차트"
                            MainTab.SETTINGS -> "설정"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = {
            Column {
                if (showAds) {
                    AdMobBanner()
                    Spacer(modifier = Modifier.height(8.dp))
                }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                )
                            },
                            label = { Text(text = tab.title) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentTab == MainTab.HOME) {
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
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (currentTab) {
                MainTab.HOME -> {
                    AssetRecordList(
                        records = records,
                        allRecords = allRecords,
                        onEdit = { record -> assetToEdit = record },
                        onDelete = { record -> recordToDelete = record },
                    )
                }
                MainTab.STATS -> {
                    StatsScreen(records = records, allRecords = allRecords)
                }
                MainTab.SETTINGS -> {
                    SettingsScreen(
                        records = records,
                        onExportCsv = ::shareCsvFile,
                        onImportCsv = {
                            csvImportLauncher.launch(arrayOf("text/*", "application/*"))
                        },
                    )
                }
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

            pendingCsvImport?.let { importedRecords ->
                AlertDialog(
                    onDismissRequest = { pendingCsvImport = null },
                    title = { Text("CSV 데이터 가져오기") },
                    text = {
                        Text(
                            if (importedRecords.isEmpty()) {
                                "가져올 기록이 없습니다."
                            } else {
                                "${importedRecords.size}개 기록을 기존 데이터에 추가할까요? 기존 데이터는 유지됩니다. 같은 파일을 다시 가져오면 중복 기록이 생길 수 있습니다."
                            },
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = importedRecords.isNotEmpty(),
                            onClick = {
                                viewModel.importRecords(importedRecords)
                                pendingCsvImport = null
                                Toast.makeText(context, "CSV 데이터를 가져왔습니다.", Toast.LENGTH_SHORT).show()
                            },
                        ) { Text("추가") }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingCsvImport = null }) { Text("취소") }
                    },
                )
            }
        }
    }
}

@Composable
fun AssetRecordList(
    records: List<AssetRecord>,
    allRecords: List<AssetRecord>,
    onEdit: (AssetRecord) -> Unit,
    onDelete: (AssetRecord) -> Unit,
) {
    val totalValue = remember(records) {
        records.sumOf { it.value ?: 0 }
    }
    val currencyFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }
    val previousRecords = remember(allRecords) {
        allRecords
            .groupBy { it.category to it.name }
            .mapValues { (_, history) ->
                history.sortedByDescending { it.date?.time ?: Long.MIN_VALUE }.getOrNull(1)
            }
    }

    val defaultCategories = remember { listOf("부동산", "금", "자동차", "입출금", "기타") }
    val groupedRecords = remember(records) {
        val map = records.groupBy { it.category.takeIf { c -> !c.isNullOrBlank() } ?: "기타" }
        map.entries.sortedWith(
            compareBy { (category, _) ->
                val idx = defaultCategories.indexOf(category)
                if (idx != -1) idx else Int.MAX_VALUE
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "home-banner") {
            HomeAssetBanner()
        }

        item(key = "asset-summary") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            shape = RoundedCornerShape(20.dp),
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
        }

        if (groupedRecords.isEmpty()) {
            item(key = "empty-home") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 42.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "아직 등록된 자산이 없어요",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "아래 + 버튼으로 첫 자산을 추가해보세요.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            items(groupedRecords, key = { (category, _) -> "category:$category" }) { (category, categoryRecords) ->
                CategorySection(
                    category = category,
                    records = categoryRecords,
                    previousRecords = previousRecords,
                    currencyFormat = currencyFormat,
                    dateFormat = dateFormat,
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
fun HomeAssetBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4F3)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .padding(start = 20.dp, top = 18.dp, end = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "Asset Manager",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    text = "나만의 자산관리",
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "자산의 흐름을 한눈에",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.asset_banner_illustration),
                contentDescription = null,
                modifier = Modifier.size(width = 112.dp, height = 100.dp),
            )
        }
    }
}

@Composable
fun CategorySection(
    category: String,
    records: List<AssetRecord>,
    previousRecords: Map<Pair<String?, String?>, AssetRecord?>,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onEdit: (AssetRecord) -> Unit,
    onDelete: (AssetRecord) -> Unit,
) {
    var expanded by rememberSaveable(category) { mutableStateOf(false) }
    val categoryTotal = remember(records) {
        records.sumOf { it.value ?: 0 }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Category Section Header (Top of Category Region)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryChip(category = category, large = true)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "합계",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "${currencyFormat.format(categoryTotal)} 원",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "${category} 접기" else "${category} 펼치기",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            // Asset records under this category
            if (expanded) {
                records.forEach { record ->
                    AssetRecordCard(
                        record = record,
                        previousRecord = previousRecords[record.category to record.name],
                        currencyFormat = currencyFormat,
                        dateFormat = dateFormat,
                        onEdit = { onEdit(record) },
                        onDelete = { onDelete(record) },
                    )
                }
            }
        }
    }
}

@Composable
fun AssetRecordCard(
    record: AssetRecord,
    previousRecord: AssetRecord?,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Line 1 (Top): Asset Name on left, Edit & Delete buttons on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = record.name ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "편집",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            val previousValue = previousRecord?.value
            val currentValue = (record.value ?: 0).toLong()
            val difference = previousRecord?.let { currentValue - (previousValue ?: 0).toLong() }
            val changeRate = difference?.let { change ->
                previousValue?.takeIf { it != 0 }?.let { change.toDouble() / it.toDouble() * 100.0 }
            }
            val changeColor = when {
                difference != null && difference > 0 -> Color(0xFFD32F2F)
                difference != null && difference < 0 -> Color(0xFF1976D2)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

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
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = buildString {
                            append(currencyFormat.format(record.value ?: 0))
                            append(" 원")
                            if (changeRate != null) {
                                append(" (")
                                append(if (changeRate > 0) "+" else "")
                                append(String.format(Locale.KOREA, "%.1f", changeRate))
                                append("%)")
                            }
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (difference == null) MaterialTheme.colorScheme.primary else changeColor,
                    )
                    if (difference != null) {
                        Text(
                            text = buildString {
                                append("전회 대비 ")
                                append(if (difference > 0) "+" else "")
                                append(currencyFormat.format(difference))
                                append("원")
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = changeColor,
                        )
                    } else {
                        Text(
                            text = "직전 기록 없음",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }

            // Line 3 (Bottom): Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "기록일",
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
fun CategoryChip(category: String, large: Boolean = false) {
    val (backgroundColor, textColor, categoryIcon) = when (category) {
        "부동산" -> Triple(Color(0xFFFCE8E7), Color(0xFF9F3030), Icons.Default.Home)
        "금" -> Triple(Color(0xFFF5F1EC), Color(0xFF6B4F3A), Icons.Default.Star)
        "자동차" -> Triple(Color(0xFFF0F0F0), Color(0xFF303030), Icons.Default.DirectionsCar)
        "입출금" -> Triple(Color(0xFFF8EEEE), Color(0xFF7D2632), Icons.Default.AccountBalanceWallet)
        else -> Triple(Color(0xFFF1F1F1), Color(0xFF505050), Icons.Default.Category)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (large) 12.dp else 8.dp, vertical = if (large) 8.dp else 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (large) 8.dp else 5.dp),
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = null,
                modifier = Modifier.size(if (large) 20.dp else 14.dp),
                tint = textColor,
            )
            Text(
                text = category,
                fontSize = if (large) 15.sp else 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
        }
    }
}

// Delete Confirmation Dialog
@Composable
fun DeleteConfirmationDialog(
    record: AssetRecord,
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
    asset: AssetRecord,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
) {
    val category = asset.category ?: ""
    val name = asset.name ?: ""

    val historyRecords by viewModel.getHistoryForAsset(category, name)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddHistoryDialog by remember { mutableStateOf(false) }
    var recordToDeleteInHistory by remember { mutableStateOf<AssetRecord?>(null) }

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
    record: AssetRecord,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onUpdateRecord: (AssetRecord) -> Unit,
    onDeleteRecord: () -> Unit,
) {
    var valueText by remember(record.value) { mutableStateOf((record.value ?: 0).toString()) }
    var currentDate by remember(record.date) { mutableStateOf(record.date ?: Date()) }
    var isEditingValue by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                    modifier = Modifier.clickable { showDatePicker = true },
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

    if (showDatePicker) {
        AssetDatePickerDialog(
            initialDate = currentDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { newDate ->
                currentDate = newDate
                onUpdateRecord(record.copy(date = newDate))
            },
        )
    }
}

@Composable
fun AddHistoryRecordDialog(
    category: String,
    name: String,
    onDismiss: () -> Unit,
    onAddRecord: (date: Date, value: Int) -> Unit,
) {
    var valueText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA) }

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
                                onClick = { showDatePicker = true },
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

    if (showDatePicker) {
        AssetDatePickerDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    existingRecords: List<AssetRecord>,
    onDismiss: () -> Unit,
    onAddRecord: (category: String, name: String, value: Int, date: Date) -> Unit,
) {
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
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA) }

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
                                onClick = { showDatePicker = true },
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

    if (showDatePicker) {
        AssetDatePickerDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate = it },
        )
    }
}

private val AssetChartPalette = listOf(
    Color(0xFFDC2626), // red
    Color(0xFF2563EB), // blue
    Color(0xFFCA8A04), // gold
    Color(0xFF15803D), // green
    Color(0xFF7E22CE), // purple
    Color(0xFF0F766E), // teal
    Color(0xFFEA580C), // orange
    Color(0xFFBE185D), // pink
    Color(0xFF0891B2), // cyan
    Color(0xFF4D7C0F), // lime
    Color(0xFF4F46E5), // indigo
    Color(0xFF475569), // slate
)

private data class DonutSlice(
    val category: String,
    val name: String?,
    val value: Int,
    val color: Color,
)

@Composable
fun StatsScreen(records: List<AssetRecord>, allRecords: List<AssetRecord>) {
    if (records.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "통계 데이터가 없습니다.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "홈 탭에서 자산을 추가하면\n카테고리별 통계와 차트를 확인할 수 있습니다.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    val currencyFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    val totalValue = remember(records) { records.sumOf { it.value ?: 0 } }
    var donutDate by remember { mutableStateOf(Date()) }
    var showDonutDatePicker by remember { mutableStateOf(false) }
    var showAssetDetails by remember { mutableStateOf(false) }

    val donutRecords = remember(allRecords, donutDate) {
        val endOfSelectedDay = Calendar.getInstance().apply {
            time = donutDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        allRecords
            .filter { it.date != null && it.date.time <= endOfSelectedDay }
            .groupBy { record ->
                val category = record.category.takeIf { !it.isNullOrBlank() } ?: "기타"
                val name = record.name.takeIf { !it.isNullOrBlank() } ?: "이름 없음"
                category to name
            }
            .values
            .mapNotNull { history -> history.maxByOrNull { it.date?.time ?: Long.MIN_VALUE } }
            .filter { it.value != null }
    }
    val donutTotal = remember(donutRecords) { donutRecords.sumOf { it.value ?: 0 } }
    val donutCategoryGroups = remember(donutRecords) {
        donutRecords
            .groupBy { it.category.takeIf { category -> !category.isNullOrBlank() } ?: "기타" }
            .mapValues { (_, categoryRecords) -> categoryRecords.sumOf { it.value ?: 0 } }
            .entries.sortedByDescending { it.value }
    }
    val chartItemKeys = remember(allRecords) {
        val defaultCategories = listOf("부동산", "금", "자동차", "입출금", "기타")
        allRecords
            .filter { !it.name.isNullOrBlank() }
            .groupBy { it.category.takeIf { category -> !category.isNullOrBlank() } ?: "기타" }
            .mapValues { (_, categoryRecords) ->
                categoryRecords.mapNotNull { it.name?.takeIf(String::isNotBlank) }.distinct()
            }
            .entries
            .sortedWith(compareBy { (category, _) ->
                val index = defaultCategories.indexOf(category)
                if (index != -1) index else Int.MAX_VALUE
            })
            .flatMap { (category, names) -> names.map { ChartItemKey(category, it) } }
    }
    val itemColors = remember(chartItemKeys) {
        chartItemKeys.mapIndexed { index, item -> item to AssetChartPalette[index % AssetChartPalette.size] }.toMap()
    }
    val donutSlices = remember(donutRecords, donutCategoryGroups, showAssetDetails, itemColors) {
        if (showAssetDetails) {
            donutRecords.map { record ->
                val category = record.category.takeIf { !it.isNullOrBlank() } ?: "기타"
                val name = record.name.takeIf { !it.isNullOrBlank() } ?: "이름 없음"
                val key = ChartItemKey(category, name)
                DonutSlice(category, name, record.value ?: 0, itemColors[key] ?: AssetChartPalette[0])
            }.sortedWith(
                compareBy<DonutSlice>(
                    { slice -> donutCategoryGroups.indexOfFirst { it.key == slice.category } },
                    { slice -> slice.name.orEmpty() },
                ),
            )
        } else {
            donutCategoryGroups.mapIndexed { index, (category, value) ->
                DonutSlice(category, null, value, AssetChartPalette[index % AssetChartPalette.size])
            }
        }
    }
    val donutValueTotal = remember(donutSlices) { donutSlices.sumOf { it.value.coerceAtLeast(0) } }
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Total Asset Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "자산 요약",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
                Text(
                    text = "${currencyFormat.format(totalValue)} 원",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // 2. Asset Line Chart Card
        AssetLineChartCard(
            allRecords = allRecords,
            currencyFormat = currencyFormat,
        )

        // 2. Donut Chart & Category Ratio Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "카테고리별 비중 차트",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("기준 날짜", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    OutlinedButton(
                        onClick = { showDonutDatePicker = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp),
                    ) {
                        Text(dateFormat.format(donutDate), fontSize = 12.sp)
                    }
                }

                FilterChip(
                    selected = showAssetDetails,
                    onClick = { showAssetDetails = !showAssetDetails },
                    label = { Text("세부 자산 항목 표시") },
                    leadingIcon = if (showAssetDetails) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else null,
                )
                if (showAssetDetails) {
                    Text(
                        text = "세부 항목의 비율은 각 카테고리 내부 기준입니다.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                // Canvas Donut Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(
                        modifier = Modifier.size(180.dp),
                    ) {
                        var startAngle = -90f
                        val strokeWidth = 36.dp.toPx()

                        if (donutValueTotal > 0) {
                            donutSlices.filter { it.value > 0 }.forEach { slice ->
                                val sweepAngle = (slice.value.toFloat() / donutValueTotal.toFloat()) * 360f

                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                                )
                                startAngle += sweepAngle
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${dateFormat.format(donutDate)} 기준",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Text(
                            text = "${currencyFormat.format(donutTotal)} 원",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Category or asset percentages
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    donutCategoryGroups.forEachIndexed { categoryIndex, (category, categorySum) ->
                        val categoryPercentage = if (donutValueTotal > 0) {
                            categorySum.coerceAtLeast(0).toDouble() / donutValueTotal * 100
                        } else 0.0
                        val categoryColor = AssetChartPalette[categoryIndex % AssetChartPalette.size]

                        if (!showAssetDetails) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(categoryColor, shape = RoundedCornerShape(3.dp)),
                                        )
                                        Text(category, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            "${currencyFormat.format(categorySum)} 원",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            "(${String.format(Locale.KOREA, "%.1f", categoryPercentage)}%)",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                        )
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { (categoryPercentage / 100.0).toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = categoryColor,
                                    trackColor = categoryColor.copy(alpha = 0.2f),
                                    drawStopIndicator = {},
                                )
                            }
                        } else {
                            val categorySlices = donutSlices.filter { it.category == category && it.value > 0 }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(category, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${currencyFormat.format(categorySum)} 원 (${String.format(Locale.KOREA, "%.1f", categoryPercentage)}%)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                }
                                val positiveCategoryTotal = categorySlices.sumOf { it.value }
                                categorySlices.forEach { slice ->
                                    val withinCategoryPercentage = if (positiveCategoryTotal > 0) {
                                        slice.value.toDouble() / positiveCategoryTotal * 100
                                    } else 0.0
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(slice.color, shape = RoundedCornerShape(2.dp)),
                                            )
                                            Text(slice.name ?: "이름 없음", fontSize = 13.sp)
                                        }
                                        Text(
                                            "${currencyFormat.format(slice.value)} 원 (${String.format(Locale.KOREA, "%.1f", withinCategoryPercentage)}%)",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDonutDatePicker) {
        AssetDatePickerDialog(
            initialDate = donutDate,
            onDismiss = { showDonutDatePicker = false },
            onDateSelected = {
                donutDate = it
                showDonutDatePicker = false
            },
        )
    }
}

enum class TimeGranularity(val label: String) {
    DAILY("일별"),
    WEEKLY("주별"),
    MONTHLY("월별"),
}

fun chartStartDateForGranularity(endDate: Date, granularity: TimeGranularity): Date =
    Calendar.getInstance().apply {
        time = endDate
        when (granularity) {
            TimeGranularity.DAILY -> set(Calendar.DAY_OF_MONTH, 1)
            TimeGranularity.WEEKLY -> add(Calendar.DAY_OF_MONTH, -29)
            TimeGranularity.MONTHLY -> add(Calendar.MONTH, -12)
        }
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

data class ChartSlot(
    val key: String,
    val label: String,
    val endTimestamp: Long,
)

private data class ChartItemKey(
    val category: String,
    val name: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDatePickerDialog(
    initialDate: Date,
    onDismiss: () -> Unit,
    onDateSelected: (Date) -> Unit,
) {
    val initialCalendar = remember(initialDate) { Calendar.getInstance().apply { time = initialDate } }
    val initialUtcMillis = remember(initialDate) {
        Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                initialCalendar.get(Calendar.YEAR),
                initialCalendar.get(Calendar.MONTH),
                initialCalendar.get(Calendar.DAY_OF_MONTH),
            )
        }.timeInMillis
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { selectedMillis ->
                        val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = selectedMillis
                        }
                        val selectedDate = Calendar.getInstance().apply {
                            clear()
                            set(
                                utcCalendar.get(Calendar.YEAR),
                                utcCalendar.get(Calendar.MONTH),
                                utcCalendar.get(Calendar.DAY_OF_MONTH),
                            )
                        }.time
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                },
            ) {
                Text("선택", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    ) {
        DatePicker(
            state = pickerState,
            showModeToggle = false,
            title = {
                Text(
                    text = "날짜 선택",
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            },
        )
    }
}

fun generateChartSlots(
    startDate: Date,
    endDate: Date,
    granularity: TimeGranularity,
): List<ChartSlot> {
    val result = mutableListOf<ChartSlot>()

    val cal = Calendar.getInstance().apply { time = startDate }
    val endCal = Calendar.getInstance().apply { time = endDate }

    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    endCal.set(Calendar.HOUR_OF_DAY, 23)
    endCal.set(Calendar.MINUTE, 59)
    endCal.set(Calendar.SECOND, 59)
    endCal.set(Calendar.MILLISECOND, 999)

    val dateFormat = SimpleDateFormat("MM.dd", Locale.KOREA)
    val monthFormat = SimpleDateFormat("yy.MM", Locale.KOREA)
    val slotKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    while (!cal.after(endCal)) {
        when (granularity) {
            TimeGranularity.DAILY -> {
                val slotEndCal = Calendar.getInstance().apply {
                    time = cal.time
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val label = dateFormat.format(cal.time)
                val key = slotKeyFormat.format(cal.time)
                result.add(ChartSlot(key, label, slotEndCal.timeInMillis))
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            TimeGranularity.WEEKLY -> {
                val slotEndCal = Calendar.getInstance().apply {
                    time = cal.time
                    add(Calendar.DAY_OF_MONTH, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val label = "${dateFormat.format(cal.time)}~"
                val key = slotKeyFormat.format(cal.time)
                result.add(ChartSlot(key, label, slotEndCal.timeInMillis))
                cal.add(Calendar.DAY_OF_MONTH, 7)
            }
            TimeGranularity.MONTHLY -> {
                val slotEndCal = Calendar.getInstance().apply {
                    time = cal.time
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val label = monthFormat.format(cal.time)
                val key = slotKeyFormat.format(cal.time)
                result.add(ChartSlot(key, label, minOf(slotEndCal.timeInMillis, endCal.timeInMillis)))
                cal.add(Calendar.MONTH, 1)
            }
        }
    }

    return if (result.size > 36) {
        result.takeLast(36)
    } else if (result.isEmpty()) {
        listOf(ChartSlot("today", dateFormat.format(Date()), System.currentTimeMillis()))
    } else {
        result
    }
}

@Composable
fun AssetLineChartCard(
    allRecords: List<AssetRecord>,
    currencyFormat: NumberFormat,
) {
    val context = LocalContext.current
    val chartPreferences = remember(context) {
        context.getSharedPreferences("asset_chart_preferences", android.content.Context.MODE_PRIVATE)
    }
    val categoryToItemsMap = remember(allRecords) {
        val defaultCategories = listOf("부동산", "금", "자동차", "입출금", "기타")
        allRecords
            .filter { !it.name.isNullOrBlank() }
            .groupBy { it.category.takeIf { c -> !c.isNullOrBlank() } ?: "기타" }
            .mapValues { (_, records) ->
                records.mapNotNull { it.name.takeIf { n -> !n.isNullOrBlank() } }.distinct()
            }
            .entries
            .sortedWith(compareBy { (cat, _) ->
                val idx = defaultCategories.indexOf(cat)
                if (idx != -1) idx else Int.MAX_VALUE
            })
    }

    val chartItems = remember(categoryToItemsMap) {
        categoryToItemsMap.flatMap { (category, names) ->
            names.map { name -> ChartItemKey(category, name) }
        }
    }

    fun itemPreferenceKey(item: ChartItemKey) = "${item.category.length}:${item.category}${item.name}"
    val knownItemKeys = remember(chartItems) { chartItems.mapTo(mutableSetOf(), ::itemPreferenceKey) }
    val savedKnownItemKeys = remember(chartPreferences) {
        chartPreferences.getStringSet("known_items", emptySet())?.toSet().orEmpty()
    }
    val savedActiveItemKeys = remember(chartPreferences) {
        chartPreferences.getStringSet("active_items", emptySet())?.toSet().orEmpty()
    }
    var activeItems: Set<ChartItemKey> by remember(chartItems, savedKnownItemKeys, savedActiveItemKeys) {
        mutableStateOf(
            chartItems.filterTo(mutableSetOf()) { item ->
                val key = itemPreferenceKey(item)
                key !in savedKnownItemKeys || key in savedActiveItemKeys
            },
        )
    }
    var showTotalLine by remember(chartPreferences) {
        mutableStateOf(chartPreferences.getBoolean("show_total_line", true))
    }
    var chartSeriesOptionsExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(activeItems, showTotalLine, knownItemKeys) {
        if (knownItemKeys.isEmpty()) return@LaunchedEffect
        val activeKeys = activeItems.mapTo(mutableSetOf(), ::itemPreferenceKey)
        chartPreferences.edit()
            .putStringSet("known_items", knownItemKeys)
            .putStringSet("active_items", activeKeys)
            .putBoolean("show_total_line", showTotalLine)
            .apply()
    }

    var granularity by remember { mutableStateOf(TimeGranularity.MONTHLY) }
    var startDate by remember {
        mutableStateOf(
            Calendar.getInstance().apply { add(Calendar.MONTH, -12) }.time
        )
    }
    var endDate by remember { mutableStateOf(Date()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val palette = AssetChartPalette

    val itemColorMap = remember(chartItems, palette) {
        chartItems.mapIndexed { index, item ->
            item to palette[index % palette.size]
        }.toMap()
    }

    val slots = remember(startDate, endDate, granularity) {
        generateChartSlots(startDate, endDate, granularity)
    }

    val itemSlotValues = remember(allRecords, chartItems, slots) {
        val map = mutableMapOf<Pair<ChartItemKey, Int>, Long>()
        slots.forEachIndexed { slotIndex, slot ->
            chartItems.forEach { item ->
                val recordsUpToSlot = allRecords.filter { record ->
                    (record.category.takeIf { category -> !category.isNullOrBlank() } ?: "기타") == item.category &&
                    record.name == item.name &&
                    record.date != null &&
                    record.date.time <= slot.endTimestamp
                }
                val latest = recordsUpToSlot.maxByOrNull { it.date?.time ?: 0L }
                map[item to slotIndex] = (latest?.value ?: 0).toLong()
            }
        }
        map
    }

    val activeItemSeries = remember(activeItems, itemSlotValues, slots) {
        activeItems.associateWith { item ->
            slots.indices.map { slotIndex ->
                itemSlotValues[item to slotIndex] ?: 0L
            }
        }
    }

    val totalSeries = remember(activeItems, itemSlotValues, slots) {
        slots.indices.map { slotIndex ->
            activeItems.sumOf { item -> itemSlotValues[item to slotIndex] ?: 0L }
        }
    }

    val allActiveValues = remember(activeItemSeries, totalSeries, showTotalLine) {
        val list = mutableListOf<Long>()
        if (showTotalLine) list.addAll(totalSeries)
        activeItemSeries.values.forEach { list.addAll(it) }
        list
    }

    val maxVal = remember(allActiveValues) { allActiveValues.maxOfOrNull { it } ?: 0L }
    val minVal = remember(allActiveValues) { allActiveValues.minOfOrNull { it } ?: 0L }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "자산 추이 그래프",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val currentTotal = totalSeries.lastOrNull() ?: 0L
                Text(
                    text = "선택 자산 합계: ${currencyFormat.format(currentTotal)} 원",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                )
            }

            // Period & Granularity Controls
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "조회 단위",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TimeGranularity.entries.forEach { g ->
                            val isSelected = granularity == g
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    granularity = g
                                    startDate = chartStartDateForGranularity(endDate, g)
                                },
                                label = { Text(g.label, fontSize = 12.sp) },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "조회 기간",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }

                        OutlinedButton(
                            onClick = {
                                showStartDatePicker = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Text(text = dateFormat.format(startDate), fontSize = 12.sp)
                        }

                        Text("~", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)

                        OutlinedButton(
                            onClick = {
                                showEndDatePicker = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Text(text = dateFormat.format(endDate), fontSize = 12.sp)
                        }
                    }
                }
            }

            HorizontalDivider(color = outlineColor.copy(alpha = 0.5f))

            // Grouped Option Chips by Category
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { chartSeriesOptionsExpanded = !chartSeriesOptionsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "그래프 표시 항목",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "합계 ${if (showTotalLine) "켜짐" else "꺼짐"} · 자산 ${activeItems.size}/${chartItems.size}개",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = if (chartSeriesOptionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (chartSeriesOptionsExpanded) "그래프 표시 항목 접기" else "그래프 표시 항목 펼치기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (chartSeriesOptionsExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = showTotalLine,
                        onClick = { showTotalLine = !showTotalLine },
                        label = {
                            Text(
                                text = "전체 합계 그래프",
                                fontWeight = if (showTotalLine) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        leadingIcon = if (showTotalLine) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else null,
                    )
                }

                categoryToItemsMap.forEach { (category, items) ->
                    val categoryItems = items.map { name -> ChartItemKey(category, name) }
                    val allCategoryActive = categoryItems.all { it in activeItems }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = {
                                activeItems = if (allCategoryActive) {
                                    activeItems - categoryItems.toSet()
                                } else {
                                    activeItems + categoryItems.toSet()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (allCategoryActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                CategoryChip(category = category)
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allCategoryActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        items.forEach { itemName ->
                            val itemKey = ChartItemKey(category, itemName)
                            val isSelected = itemKey in activeItems
                            val chipColor = itemColorMap[itemKey] ?: primaryColor

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeItems = if (isSelected) {
                                        activeItems - itemKey
                                    } else {
                                        activeItems + itemKey
                                    }
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    color = if (isSelected) chipColor else chipColor.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(2.dp),
                                                ),
                                        )
                                        Text(
                                            text = itemName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
                }
            }

            HorizontalDivider(color = outlineColor.copy(alpha = 0.5f))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val paddingLeft = 68.dp.toPx()
                    val paddingRight = 12.dp.toPx()
                    val paddingTop = 20.dp.toPx()
                    val paddingBottom = 30.dp.toPx()

                    val chartWidth = size.width - paddingLeft - paddingRight
                    val chartHeight = size.height - paddingTop - paddingBottom

                    val range = if (maxVal == minVal) 1L else (maxVal - minVal)

                    val gridCount = 3
                    val axisLabelPaint = android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    val guidePaint = android.graphics.Paint().apply {
                        color = outlineColor.copy(alpha = 0.65f).toArgb()
                        strokeWidth = 1.dp.toPx()
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()), 0f)
                        isAntiAlias = true
                    }
                    for (i in 0..gridCount) {
                        val gy = paddingTop + (i.toFloat() / gridCount) * chartHeight
                        drawLine(
                            color = outlineColor.copy(alpha = 0.3f),
                            start = Offset(paddingLeft, gy),
                            end = Offset(paddingLeft + chartWidth, gy),
                            strokeWidth = 1.dp.toPx(),
                        )
                        val tickValue = maxVal.toDouble() -
                            (i.toDouble() / gridCount) * (maxVal.toDouble() - minVal.toDouble())
                        val tickLabel = when {
                            tickValue >= 100_000_000 -> String.format(Locale.KOREA, "%.1f억", tickValue / 100_000_000)
                            tickValue >= 10_000 -> String.format(Locale.KOREA, "%.0f만", tickValue / 10_000)
                            else -> NumberFormat.getIntegerInstance(Locale.KOREA).format(tickValue.toLong())
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            tickLabel,
                            paddingLeft - 8.dp.toPx(),
                            gy + axisLabelPaint.textSize / 3f,
                            axisLabelPaint,
                        )
                    }

                    val slotCount = slots.size

                    // 1. Draw Active Individual Item Lines
                    activeItemSeries.forEach { (item, values) ->
                        val color = itemColorMap[item] ?: primaryColor
                        val points = values.mapIndexed { index, value ->
                            val x = paddingLeft + (index.toFloat() / (slotCount - 1).coerceAtLeast(1)) * chartWidth
                            val normalized = ((value - minVal).toFloat() / range.toFloat())
                            val y = paddingTop + chartHeight - (normalized * chartHeight)
                            Offset(x, y)
                        }

                        points.forEach { point ->
                            drawContext.canvas.nativeCanvas.drawLine(
                                point.x,
                                point.y,
                                point.x,
                                paddingTop + chartHeight,
                                guidePaint,
                            )
                        }

                        if (points.size > 1) {
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = color,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                            )
                        }

                        points.forEach { point ->
                            drawCircle(
                                color = color,
                                radius = 3.dp.toPx(),
                                center = point,
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.5.dp.toPx(),
                                center = point,
                            )
                        }
                    }

                    // 2. Draw Combined Total Line (if enabled)
                    if (showTotalLine && totalSeries.isNotEmpty()) {
                        val totalPoints = totalSeries.mapIndexed { index, value ->
                            val x = paddingLeft + (index.toFloat() / (slotCount - 1).coerceAtLeast(1)) * chartWidth
                            val normalized = ((value - minVal).toFloat() / range.toFloat())
                            val y = paddingTop + chartHeight - (normalized * chartHeight)
                            Offset(x, y)
                        }

                        totalPoints.forEach { point ->
                            drawContext.canvas.nativeCanvas.drawLine(
                                point.x,
                                point.y,
                                point.x,
                                paddingTop + chartHeight,
                                guidePaint,
                            )
                        }

                        if (totalPoints.size > 1) {
                            val fillPath = Path().apply {
                                moveTo(totalPoints.first().x, paddingTop + chartHeight)
                                totalPoints.forEach { pt -> lineTo(pt.x, pt.y) }
                                lineTo(totalPoints.last().x, paddingTop + chartHeight)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.25f),
                                        Color.Transparent,
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight,
                                ),
                            )

                            val linePath = Path().apply {
                                moveTo(totalPoints.first().x, totalPoints.first().y)
                                for (i in 1 until totalPoints.size) {
                                    lineTo(totalPoints[i].x, totalPoints[i].y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = primaryColor,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
                            )
                        }

                        totalPoints.forEach { point ->
                            drawCircle(
                                color = primaryColor,
                                radius = 4.5.dp.toPx(),
                                center = point,
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = point,
                            )
                        }
                    }

                    // X-axis Slot Labels
                    val labelPaint = android.graphics.Paint().apply {
                        color = textColor.toArgb()
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val labelStep = when {
                        slotCount <= 12 -> 1
                        slotCount <= 20 -> 2
                        else -> 4
                    }

                    slots.forEachIndexed { index, slot ->
                        val x = paddingLeft + (index.toFloat() / (slotCount - 1).coerceAtLeast(1)) * chartWidth
                        if (index % labelStep == 0 || index == slotCount - 1) {
                            drawContext.canvas.nativeCanvas.drawText(
                                slot.label,
                                x,
                                paddingTop + chartHeight + 20.dp.toPx(),
                                labelPaint,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        AssetDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { selected ->
                if (selected.after(endDate)) {
                    startDate = selected
                    endDate = selected
                } else {
                    startDate = selected
                }
            },
        )
    }

    if (showEndDatePicker) {
        AssetDatePickerDialog(
            initialDate = endDate,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { selected ->
                if (selected.before(startDate)) {
                    startDate = selected
                    endDate = selected
                } else {
                    endDate = selected
                }
            },
        )
    }
}

@Composable
fun SettingsScreen(
    records: List<AssetRecord>,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // App Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "나만의 자산관리 Asset Manager",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "개인 자산을 효율적으로 관리하고 통계를 확인해보세요.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }

        // Asset Management & Data Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "데이터 관리",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "보유 자산 항목 수",
                        fontSize = 15.sp,
                    )
                    Text(
                        text = "${records.size}개",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = "전체 기록을 CSV 파일로 공유하거나 파일에서 추가할 수 있어요.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onExportCsv,
                    ) {
                        Text("CSV 공유")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onImportCsv,
                    ) {
                        Text("CSV 가져오기")
                    }
                }
                Text(
                    text = "CSV 열 형식: category, name, value, date (ISO 8601, 예: 2026-10-06T14:30:00+09:00). 기존 데이터에 추가됩니다.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                )

            }
        }

        // Display & Currency Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "표시 및 카테고리 설정",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "기본 통화",
                        fontSize = 15.sp,
                    )
                    Text(
                        text = "대한민국 원 (KRW, ₩)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }

        // App Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "앱 정보",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "버전 정보",
                        fontSize = 15.sp,
                    )
                    Text(
                        text = "1.1.0",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }

}
