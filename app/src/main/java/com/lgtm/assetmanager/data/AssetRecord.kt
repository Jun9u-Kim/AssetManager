package com.lgtm.assetmanager.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "asset_records")
data class AssetRecord(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "value") val value: Int?,
    @ColumnInfo(name = "date") val date: Date?,
)
