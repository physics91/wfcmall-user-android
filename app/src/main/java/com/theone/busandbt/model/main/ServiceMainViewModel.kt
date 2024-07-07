package com.theone.busandbt.model.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.promotion.PromotionListItem

class ServiceMainViewModel(private val state: SavedStateHandle) : ViewModel() {
    val serviceTabPositionLiveData: LiveData<Int> = state.getLiveData("serviceTabPosition")
    val promotionListLiveData: LiveData<List<PromotionListItem>> =
        state.getLiveData("promotionList")
    val scrollYLiveData: LiveData<Int> = state.getLiveData("scrollY")
    val isBannerFormVisibleLiveData: LiveData<Boolean> = state.getLiveData("isBannerFormVisible")

    fun setServiceTabPosition(newPosition: Int) {
        state["serviceTabPosition"] = newPosition
    }

    fun setPromotionList(data: List<PromotionListItem>) {
        state["promotionList"] = data
    }

    fun setScrollY(scrollY: Int) {
        state["scrollY"] = scrollY
    }

    fun setBannerFormVisible(isBannerFormVisible: Boolean) {
        state["isBannerFormVisible"] = isBannerFormVisible
    }
}