package com.theone.busandbt.model.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.order.MallOrderDetail

class MallOrderDetailViewModel(private val state: SavedStateHandle) : ViewModel() {
    val mallOrderDetailLiveData: LiveData<MallOrderDetail> = state.getLiveData("mallOrderDetail")

    fun setMallOrderDetail(orderDetail: MallOrderDetail) {
        state["mallOrderDetail"] = orderDetail
    }
}