package com.theone.busandbt.model.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.shop.DiscountShopListItem
import com.theone.busandbt.dto.shop.NewShopListItem
import com.theone.busandbt.dto.shop.PopularShopListItem

class FoodInstantMainViewModel(private val state: SavedStateHandle) : ViewModel() {
    val popularShopListLiveData: LiveData<List<PopularShopListItem>> =
        state.getLiveData("popularShopList")
    val discountShopListLiveData: LiveData<List<DiscountShopListItem>> =
        state.getLiveData("discountShopList")
    val newShopListLiveData: LiveData<List<NewShopListItem>> = state.getLiveData("newShopList")

    fun setPopularShopList(data: List<PopularShopListItem>) {
        state["popularShopList"] = data
    }

    fun setDiscountShopList(data: List<DiscountShopListItem>) {
        state["discountShopList"] = data
    }

    fun setNewShopList(data: List<NewShopListItem>) {
        state["newShopList"] = data
    }
}