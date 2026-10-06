package com.lgtm.assetmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lgtm.assetmanager.data.AppDatabase
import com.lgtm.assetmanager.data.AssetRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).assetRecordDao()

    // Filter to expose only the most recent date record for each (category, name) pair
    val records: StateFlow<List<AssetRecord>> = dao.getAll()
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

    val allRecords: StateFlow<List<AssetRecord>> = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

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
        }
    }

    fun updateRecord(record: AssetRecord) {
        viewModelScope.launch {
            dao.update(record)
        }
    }

    fun updateAssetCategoryAndName(oldCategory: String, oldName: String, newCategory: String, newName: String) {
        viewModelScope.launch {
            dao.updateAssetCategoryAndName(oldCategory, oldName, newCategory, newName)
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
