package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType
import com.busandbt.code.UserType

/**
 * 로그아웃 요청 데이터
 * @param userId 로그아웃할 사용자 고유번호, 여기선 memberId와 같다.
 */
data class LogoutRequest(
    val userId: Int,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val userType: Int = UserType.MEMBER.id
)