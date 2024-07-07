package com.theone.busandbt.model.basket

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class BasketMainViewModel(private val state: SavedStateHandle) : ViewModel() {
    val serviceTabPositionLiveData: LiveData<Int> = state.getLiveData("serviceTabPosition")

    fun setServiceTabPosition(newPosition: Int) {
        state["serviceTabPosition"] = newPosition
    }
}