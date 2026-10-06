package com.lgtm.assetmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetRecordDao {
    @Query("SELECT * FROM asset_records ORDER BY date DESC")
    fun getAll(): Flow<List<AssetRecord>>

    @Query("SELECT * FROM asset_records WHERE uid = :uid")
    suspend fun getById(uid: Int): AssetRecord?

    @Query("SELECT * FROM asset_records WHERE category = :category AND name = :name ORDER BY date DESC")
    fun getRecordsByCategoryAndName(category: String, name: String): Flow<List<AssetRecord>>

    @Query("UPDATE asset_records SET category = :newCategory, name = :newName WHERE category = :oldCategory AND name = :oldName")
    suspend fun updateAssetCategoryAndName(oldCategory: String, oldName: String, newCategory: String, newName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AssetRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg records: AssetRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AssetRecord>)

    @Query(
        """
        DELETE FROM asset_records
        WHERE uid NOT IN (
            SELECT MIN(uid)
            FROM asset_records
            GROUP BY category, name, value, date
        )
        """,
    )
    suspend fun removeDuplicateRecords()

    @Update
    suspend fun update(record: AssetRecord)

    @Delete
    suspend fun delete(record: AssetRecord)

    @Query("DELETE FROM asset_records")
    suspend fun deleteAll()
}
