package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType
import com.busandbt.code.UserType

/**
 * 회원 비밀번호 변경 요청 데이터
 * @param currentPassword 현재 비밀번호
 * @param newPassword 새 비밀번호
 */
data class SetPasswordRequest(
    val loginId: String,
    val currentPassword: String,
    val newPassword: String,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val userType: Int = UserType.MEMBER.id
)