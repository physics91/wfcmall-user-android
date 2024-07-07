package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType

/**
 * 사용자 아이디 중복 체크 요청 객체
 * @param loginId 체크할 로그인 아이디
 */
data class CheckLoginIdRequest(
    val loginId: String,
    val channelType: Int = ChannelType.DONG_BAEK.id
)