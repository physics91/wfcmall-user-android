package com.theone.busandbt.model.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.shop.ShopDetail

class ShopDetailViewModel(private val state: SavedStateHandle) : ViewModel() {
    val shopDetailLiveData: LiveData<ShopDetail> = state.getLiveData("shopDetail")
    var deliveryTypeTabPosition: Int? = null
    var menuGroupTabPosition: Int? = null

    fun setShopDetail(shopDetail: ShopDetail) {
        state["shopDetail"] = shopDetail
    }
}