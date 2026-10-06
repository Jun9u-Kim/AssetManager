package com.sample.assetmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceRecordDao {
    @Query("SELECT * FROM finance_records ORDER BY date DESC")
    fun getAll(): Flow<List<FinanceRecord>>

    @Query("SELECT * FROM finance_records WHERE uid = :uid")
    suspend fun getById(uid: Int): FinanceRecord?

    @Query("SELECT * FROM finance_records WHERE category = :category AND name = :name ORDER BY date DESC")
    fun getRecordsByCategoryAndName(category: String, name: String): Flow<List<FinanceRecord>>

    @Query("UPDATE finance_records SET category = :newCategory, name = :newName WHERE category = :oldCategory AND name = :oldName")
    suspend fun updateAssetCategoryAndName(oldCategory: String, oldName: String, newCategory: String, newName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FinanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg records: FinanceRecord)

    @Update
    suspend fun update(record: FinanceRecord)

    @Delete
    suspend fun delete(record: FinanceRecord)

    @Query("DELETE FROM finance_records")
    suspend fun deleteAll()
}
