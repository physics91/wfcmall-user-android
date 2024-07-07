package com.theone.busandbt.model.main

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.notice.NoticeFixedListItem

/**
 * 메인 화면의 ViewModel을 정의한다.
 */
class MainViewModel : ViewModel() {
    private val fixedNoticeListLiveData: MutableLiveData<List<NoticeFixedListItem>> =
        MutableLiveData()
    private val currentFixedNoticeLiveData = MutableLiveData<NoticeFixedListItem>()
    val fixedNoticeList: List<NoticeFixedListItem>? get() = fixedNoticeListLiveData.value
    val currentFixedNotice: NoticeFixedListItem? get() = currentFixedNoticeLiveData.value

    fun setFixedNoticeList(data: List<NoticeFixedListItem>) {
        fixedNoticeListLiveData.value = data
    }

    fun setCurrentFixedNotice(position: Int) {
        val list = fixedNoticeList ?: return
        if (position > list.size - 1) return
        currentFixedNoticeLiveData.value = list[position]
    }

    fun observe(lifecycleOwner: LifecycleOwner, op: (List<NoticeFixedListItem>) -> Unit) {
        fixedNoticeListLiveData.observe(lifecycleOwner) {
            op(it)
        }
    }

    fun observeCurrentFixedNotice(lifecycleOwner: LifecycleOwner, op: (NoticeFixedListItem?) -> Unit) {
        currentFixedNoticeLiveData.observe(lifecycleOwner) {
            op(it)
        }
    }
}