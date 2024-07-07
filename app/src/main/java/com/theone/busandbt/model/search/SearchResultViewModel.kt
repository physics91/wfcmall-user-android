package com.theone.busandbt.model.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.busandbt.code.MenuSortType
import com.busandbt.code.ShopSortType

data class SearchResultViewModel(private val state: SavedStateHandle) : ViewModel() {
    val tabPositionLiveData: LiveData<Int> = state.getLiveData("tabPosition")
    val keywordLiveData: LiveData<String?> = state.getLiveData("keyword")
    val minOrderCostLiveData: LiveData<Int?> = state.getLiveData("minOrderCost")
    val hasCouponLiveData: LiveData<Boolean?> = state.getLiveData("hasCoupon")
    val canPackagingLiveData: LiveData<Boolean?> = state.getLiveData("canPackaging")
    val shopSortTypeLiveData: LiveData<ShopSortType?> = state.getLiveData("shopSortType")
    val menuSortTypeLiveData: LiveData<MenuSortType?> = state.getLiveData("menuSortType")
    var savedTabPosition = 0

    fun setTabPosition(tabPosition: Int) {
        state["tabPosition"] = tabPosition
    }

    fun setKeyword(keyword: String?) {
        if (keywordLiveData.value != keyword) {
            state["keyword"] = keyword
        }
    }

    fun setMinOrdercost(minOrderCost: Int?) {
        state["minOrderCost"] = minOrderCost
    }

    fun setHasCoupon(hasCoupon: Boolean?) {
        state["hasCoupon"] = hasCoupon
    }

    fun setCanPackaging(canPackaging: Boolean?) {
        state["canPackaging"] = canPackaging
    }

    fun setShopSortType(shopSortType: ShopSortType?) {
        state["shopSortType"] = shopSortType
    }

    fun setMenuSortType(menuSortType: MenuSortType?) {
        state["menuSortType"] = menuSortType
    }
}