package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType
import com.busandbt.code.Gender
import com.busandbt.code.UserType

/**
 * 회원 가입 요청 데이터
 * @param telecomType [TelecomType]
 * @param tel 여기서는 [loginId]와 동일하다.
 */
data class JoinRequest(
    val loginId: String,
    val loginPassword: String,
    val name: String,
    val nickname: String,
    val telecomType: Int,
    val tel: String,
    val birth: String,
    val email: String,
    val gender: Int = Gender.WOMAN.id,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val userType: Int = UserType.MEMBER.id
)