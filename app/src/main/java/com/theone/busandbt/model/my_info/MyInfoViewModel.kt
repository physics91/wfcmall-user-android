package com.theone.busandbt.model.my_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.theone.busandbt.dto.member.MemberDetail

class MyInfoViewModel(private val state: SavedStateHandle) : ViewModel() {
    val memberDetailLiveData: LiveData<MemberDetail> = state.getLiveData("memberDetail")

    fun setMemberDetail(memberDetail: MemberDetail) {
        state["memberDetail"] = memberDetail
    }
}