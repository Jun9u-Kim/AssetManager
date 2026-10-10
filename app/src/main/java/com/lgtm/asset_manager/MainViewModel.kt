package com.lgtm.asset_manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.lgtm.asset_manager.data.AppDatabase
import com.lgtm.asset_manager.data.AssetRecord
import com.lgtm.asset_manager.data.ExpenseEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.assetRecordDao()
    private val expenseDao = database.expenseEntryDao()

    val expenseEntries: StateFlow<List<ExpenseEntry>> = expenseDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpenseEntry(entry: ExpenseEntry) {
        viewModelScope.launch {
            database.withTransaction {
                expenseDao.insert(entry)
                if (entry.type == "저축" && entry.includeInAssets) {
                    applyAssetDelta(entry, entry.amount)
                    if (entry.includeInFutureAssets) applyDeltaToFutureRecords(entry, entry.amount)
                }
            }
        }
    }

    fun deleteExpenseEntry(entry: ExpenseEntry) {
        viewModelScope.launch { expenseDao.delete(entry) }
    }

    fun updateExpenseEntry(entry: ExpenseEntry) {
        viewModelScope.launch { expenseDao.update(entry) }
    }

    /** Add a dated balance snapshot using only the latest snapshot on or before the entry date. */
    private suspend fun applyAssetDelta(entry: ExpenseEntry, delta: Long) {
        if (delta == 0L) return
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, entry.day / 10000)
            set(Calendar.MONTH, (entry.day / 100) % 100 - 1)
            set(Calendar.DAY_OF_MONTH, entry.day % 100)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val cutoff = Date(calendar.timeInMillis)
        val current = dao.getMostRecentRecordOnOrBefore(entry.category, entry.title, cutoff)?.value?.toLong() ?: 0L
        val updated = (current + delta).coerceIn(0L, Int.MAX_VALUE.toLong())
        dao.insert(AssetRecord(category = entry.category, name = entry.title, value = updated.toInt(), date = cutoff))
    }

    private suspend fun applyDeltaToFutureRecords(entry: ExpenseEntry, delta: Long) {
        if (delta == 0L) return
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, entry.day / 10000)
            set(Calendar.MONTH, (entry.day / 100) % 100 - 1)
            set(Calendar.DAY_OF_MONTH, entry.day % 100)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        dao.getRecordsAfter(entry.category, entry.title, Date(calendar.timeInMillis)).forEach { record ->
            val updated = ((record.value ?: 0).toLong() + delta).coerceIn(0L, Int.MAX_VALUE.toLong())
            dao.update(record.copy(value = updated.toInt()))
        }
    }

    fun importExpenseEntries(entries: List<ExpenseEntry>) {
        if (entries.isEmpty()) return
        viewModelScope.launch { expenseDao.insertAll(entries) }
    }

    init {
        viewModelScope.launch {
            dao.removeDuplicateRecords()
        }
    }

    // Filter to expose only the most recent date record for each (category, name) pair
    val records: StateFlow<List<AssetRecord>> = dao.getAll()
        .map { list ->
            val todayCutoff = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            list.filter { record -> record.date == null || record.date.time <= todayCutoff }
                .groupBy { (_, category, name) -> category to name }
                .values
                .mapNotNull { groupList ->
                    groupList.maxWithOrNull(compareBy<AssetRecord> { it.date?.time ?: Long.MIN_VALUE }.thenBy { it.uid })
                }
                .sortedByDescending { record -> record.date?.time ?: Long.MIN_VALUE }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val allRecords: StateFlow<List<AssetRecord>> = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    suspend fun getAllRecordsSnapshot(): List<AssetRecord> = dao.getAll().first()

    suspend fun getExpenseEntriesSnapshot(): List<ExpenseEntry> = expenseDao.getAll().first()

    fun getHistoryForAsset(category: String, name: String): Flow<List<AssetRecord>> {
        return dao.getRecordsByCategoryAndName(category, name)
    }

    fun addRecord(category: String, name: String, value: Int?, date: Date?) {
        viewModelScope.launch {
            dao.insert(
                AssetRecord(
                    category = category,
                    name = name,
                    value = value,
                    date = date,
                ),
            )
            dao.removeDuplicateRecords()
        }
    }

    fun importRecords(records: List<AssetRecord>) {
        if (records.isEmpty()) return
        viewModelScope.launch {
            dao.insertRecords(records)
            dao.removeDuplicateRecords()
        }
    }

    fun updateRecord(record: AssetRecord) {
        viewModelScope.launch {
            dao.update(record)
            dao.removeDuplicateRecords()
        }
    }

    fun updateAssetCategoryAndName(oldCategory: String, oldName: String, newCategory: String, newName: String) {
        viewModelScope.launch {
            dao.updateAssetCategoryAndName(oldCategory, oldName, newCategory, newName)
            dao.removeDuplicateRecords()
        }
    }

    fun deleteRecord(record: AssetRecord) {
        viewModelScope.launch {
            dao.delete(record)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}
