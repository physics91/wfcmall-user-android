package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType
import com.busandbt.code.UserType

/**
 * 자동 로그인 요청 데이터
 * @param userId 로그인 대상 고유번호, 여기서는 memberId가 된다.
 *
 */
data class LoginByTokenRequest(
    val userId: Int,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val userType: Int = UserType.MEMBER.id
)