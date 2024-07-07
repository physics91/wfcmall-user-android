package com.theone.busandbt.model.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.menu.NewAndPopularMallMenuListItem
import com.theone.busandbt.dto.shop.DiscountShopListItem

class MallMainViewModel(private val state: SavedStateHandle) : ViewModel() {
    val popularMenuListLiveData: LiveData<List<NewAndPopularMallMenuListItem>> =
        state.getLiveData("popularMenuList")
    val discountShopListLiveData: LiveData<List<DiscountShopListItem>> =
        state.getLiveData("discountShopList")
    val newMenuListLiveData: LiveData<List<NewAndPopularMallMenuListItem>> = state.getLiveData("newMenuList")

    fun setPopularMenuList(data: List<NewAndPopularMallMenuListItem>) {
        state["popularMenuList"] = data
    }

    fun setDiscountShopList(data: List<DiscountShopListItem>) {
        state["discountShopList"] = data
    }

    fun setNewMenuList(data: List<NewAndPopularMallMenuListItem>) {
        state["newMenuList"] = data
    }
}