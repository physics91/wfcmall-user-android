package com.theone.busandbt.model.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.busandbt.code.MenuSortType

class MallMenuListViewModel(private val state: SavedStateHandle) : ViewModel() {
    val menuSortTypeLiveData: LiveData<MenuSortType> = state.getLiveData("menuSortType", MenuSortType.RECENT_ADD)
    val hasCouponLiveData: LiveData<Boolean> = state.getLiveData("hasCoupon")
    val menuSortType: MenuSortType? get() = menuSortTypeLiveData.value
    val hasCoupon: Boolean? get() = hasCouponLiveData.value
    val tabPositionLiveData: LiveData<Int> = state.getLiveData("tabPosition")
    val tabPosition: Int? get() = tabPositionLiveData.value

    fun setMenuSortType(menuSortType: MenuSortType) {
        state["menuSortType"] = menuSortType
    }

    fun setHasCoupon(hasCoupon: Boolean?) {
        state["hasCoupon"] = hasCoupon
    }

    fun setTabPosition(tabPosition: Int) {
        state["tabPosition"] = tabPosition
    }
}