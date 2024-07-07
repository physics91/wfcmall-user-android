package com.theone.busandbt.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.promotion.PromotionListItem

/**
 * currentPosition 라이브 데이터
 * fragment 특성상 화면 전환간에 view가 늘 새롭게 생성되기 되기 때문에
 * 이미지 배너의 현재 position을 알고 있지 않으면 position은 늘 0이라 화면 전환할 때마다 이미지 배너는 0번째 부터 swipe를 시작할 것입니다
 */
class BannerViewModel : ViewModel() {
    private val _bannerItemList: MutableLiveData<List<PromotionListItem>> = MutableLiveData()
    private val _currentPosition: MutableLiveData<Int> = MutableLiveData()

    val bannerItemList: LiveData<List<PromotionListItem>>
        get() = _bannerItemList
    val currentPosition: LiveData<Int>
        get() = _currentPosition

    init {
        _currentPosition.value = 0
    }

    fun setBannerItems(list: List<PromotionListItem>) {
        _bannerItemList.value = list
    }

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    fun getCurrentPosition() = currentPosition.value
}