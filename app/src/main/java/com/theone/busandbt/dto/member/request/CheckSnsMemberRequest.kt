package com.theone.busandbt.dto.member.request

import com.busandbt.code.ChannelType

data class CheckSnsMemberRequest(
    val joinType: Int,
    val snsClientId: String,
    val channelType: ChannelType = ChannelType.DONG_BAEK
)