package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType
import com.busandbt.code.UserType

/**
 * 회원 비밀번호 강제변경 요청 객체
 */
class SetPasswordForcedRequest(
    val loginId: String,
    val newPassword: String,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val userType: Int = UserType.MEMBER.id
)