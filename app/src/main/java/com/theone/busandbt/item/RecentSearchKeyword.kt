package com.theone.busandbt.item

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 최근검색어 아이템
 */
@Entity(tableName = "search_table")
data class RecentSearchKeyword(
    @PrimaryKey @ColumnInfo(name = "search_history") val keyword: String
) {
    @ColumnInfo(name = "date")
    var date: Long = System.currentTimeMillis()
}