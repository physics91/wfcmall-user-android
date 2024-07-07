package com.theone.busandbt.model

import android.app.Application
import androidx.lifecycle.*
import com.theone.busandbt.database.SearchHistoryDB
import com.theone.busandbt.item.RecentSearchKeyword
import com.theone.busandbt.repository.SearchHistoryListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 최근검색어 아이템 리스트 뷰모델
 */
class RecentSearchKeywordViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData : LiveData<List<RecentSearchKeyword>>
    private val repository : SearchHistoryListRepository
    var getSearch: RecentSearchKeyword? = null

    init {
        val searchHistoryDao = SearchHistoryDB.getDatabase(application)!!.searchHistoryDao()
        repository = SearchHistoryListRepository(searchHistoryDao)
        readAllData = repository.readAllData
    }

    //추가
    fun addSearch(searchItem : RecentSearchKeyword){
        getSearch = searchItem
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSearch(searchItem)
        }
    }
    //삭제
    fun deleteSearch(searchItem : RecentSearchKeyword){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSearch(searchItem)
        }
    }
    //전체삭제
    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
    // ViewModel에 파라미터를 넘기기 위해서, 파라미터를 포함한 Factory 객체를 생성하기 위한 클래스
    class Factory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecentSearchKeywordViewModel(application) as T
        }
    }
}