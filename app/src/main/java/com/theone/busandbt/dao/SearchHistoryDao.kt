package com.theone.busandbt.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.theone.busandbt.item.RecentSearchKeyword

/**
 * (최근검색어) SQL 쿼리를 지정하여 메서드 호출과 연결
 */
@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_table order by date desc")
    fun readAllData(): LiveData<List<RecentSearchKeyword>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: RecentSearchKeyword)

    @Delete
    suspend fun deleteSearch(search: RecentSearchKeyword)

    @Query("DELETE FROM search_table")
    suspend fun deleteAll()
}