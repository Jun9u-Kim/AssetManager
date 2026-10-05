package com.sample.financemanager.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "finance_records")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "value") val value: Int?,
    @ColumnInfo(name = "date") val date: Date?,
)
