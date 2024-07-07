package com.theone.busandbt.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SingleListFragmentViewModel<Item>(private val state: SavedStateHandle) : ViewModel() {
    val itemListLiveData: LiveData<List<Item>> = state.getLiveData("itemList")
    val currentPageLiveData: LiveData<Int> = state.getLiveData("currentPage")
    val allDataLoadedLiveData: LiveData<Boolean> = state.getLiveData("allDataLoaded")
    var saved = false

    fun setItemList(itemList: List<Item>) {
        state["itemList"] = itemList
    }

    fun setCurrentPage(currentPage: Int) {
        state["currentPage"] = currentPage
    }

    fun setAllDataLoaded(isDataEnd: Boolean) {
        state["allDataLoaded"] = isDataEnd
    }
}