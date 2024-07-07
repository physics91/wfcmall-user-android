package com.theone.busandbt.model.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.shop.MallShopDetail

class MallShopDetailViewModel(private val state: SavedStateHandle) : ViewModel() {
    val shopDetailLiveData: LiveData<MallShopDetail> = state.getLiveData("shopDetail")

    fun setShopDetail(shopDetail: MallShopDetail) {
        state["shopDetail"] = shopDetail
    }
}