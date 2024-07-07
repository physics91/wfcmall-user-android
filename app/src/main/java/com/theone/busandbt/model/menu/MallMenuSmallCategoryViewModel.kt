package com.theone.busandbt.model.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MallMenuSmallCategoryViewModel(private val state: SavedStateHandle) : ViewModel() {
    val menuCountLiveData: LiveData<Int> = state.getLiveData("menuCount")
    var savedTabPosition: Int? = null

    fun setMenuCount(menuCount: Int) {
        state["menuCount"] = menuCount
    }
}