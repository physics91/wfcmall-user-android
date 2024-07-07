package com.theone.busandbt.model.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class OrderListMainViewModel(private val state: SavedStateHandle) : ViewModel() {
    val tabPositionLiveData: LiveData<Int> = state.getLiveData("tabPosition")
    var writtenReviewOrderId: LiveData<String> = state.getLiveData("writtenReviewOrderId")
    var savedTabPosition: Int? = null

    fun setTabPosition(newPosition: Int) {
        state["tabPosition"] = newPosition
    }

    fun setWrittenReviewOrderId(orderId: String) {
        state["writtenReviewOrderId"] = orderId
    }
}