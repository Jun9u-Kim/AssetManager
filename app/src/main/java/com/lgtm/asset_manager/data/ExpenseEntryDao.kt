package com.lgtm.asset_manager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseEntryDao {
    @Query("SELECT * FROM expense_entries ORDER BY day DESC, id DESC")
    fun getAll(): Flow<List<ExpenseEntry>>

    @Query("SELECT * FROM expense_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExpenseEntry?

    @Insert
    suspend fun insert(entry: ExpenseEntry)

    @Insert
    suspend fun insertAll(entries: List<ExpenseEntry>)

    @Update
    suspend fun update(entry: ExpenseEntry)

    @Delete
    suspend fun delete(entry: ExpenseEntry)
}
