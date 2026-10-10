package com.lgtm.asset_manager.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense_entries", indices = [Index(value = ["day"])])
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Local calendar day stored as yyyyMMdd, independent of time zone. */
    val day: Int,
    /** One of 수입, 지출, 저축. */
    val type: String,
    val category: String,
    val title: String,
    val amount: Long,
    val memo: String = "",
    val includeInAssets: Boolean = false,
    val includeInFutureAssets: Boolean = false,
)
