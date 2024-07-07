package com.theone.busandbt.repository

import androidx.lifecycle.LiveData
import com.theone.busandbt.dao.SearchHistoryDao
import com.theone.busandbt.item.RecentSearchKeyword

/***
 * (최근검색어) 앱에서 사용하는 데이터와 그 데이터 통신을 하는 역할
 */
class SearchHistoryListRepository(private val searchHistoryDao: SearchHistoryDao) {

    val readAllData: LiveData<List<RecentSearchKeyword>> = searchHistoryDao.readAllData()

    suspend fun addSearch(searchHistory: RecentSearchKeyword) {
        searchHistoryDao.insert(searchHistory)
    }

    suspend fun deleteSearch(searchHistory: RecentSearchKeyword) {
        searchHistoryDao.deleteSearch(searchHistory)
    }

    suspend fun deleteAll() {
        searchHistoryDao.deleteAll()
    }
}