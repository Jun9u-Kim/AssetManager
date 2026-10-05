package com.sample.financemanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sample.financemanager.data.AppDatabase
import com.sample.financemanager.data.FinanceRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).financeRecordDao()

    // Filter to expose only the most recent date record for each (category, name) pair
    val records: StateFlow<List<FinanceRecord>> = dao.getAll()
        .map { list ->
            list.groupBy { (_, category, name) -> category to name }
                .values
                .mapNotNull { groupList ->
                    groupList.maxByOrNull { record -> record.date?.time ?: Long.MIN_VALUE }
                }
                .sortedByDescending { record -> record.date?.time ?: Long.MIN_VALUE }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun getHistoryForAsset(category: String, name: String): Flow<List<FinanceRecord>> {
        return dao.getRecordsByCategoryAndName(category, name)
    }

    fun addRecord(category: String, name: String, value: Int?, date: Date?) {
        viewModelScope.launch {
            dao.insert(
                FinanceRecord(
                    category = category,
                    name = name,
                    value = value,
                    date = date,
                ),
            )
        }
    }

    fun updateRecord(record: FinanceRecord) {
        viewModelScope.launch {
            dao.update(record)
        }
    }

    fun updateAssetCategoryAndName(oldCategory: String, oldName: String, newCategory: String, newName: String) {
        viewModelScope.launch {
            dao.updateAssetCategoryAndName(oldCategory, oldName, newCategory, newName)
        }
    }

    fun deleteRecord(record: FinanceRecord) {
        viewModelScope.launch {
            dao.delete(record)
        }
    }
}
