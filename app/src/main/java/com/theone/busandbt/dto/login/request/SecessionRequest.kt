package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType
import com.busandbt.code.UserType

/**
 * 회원탈퇴 요청 데이터
 * @param userId 사용자 고유번호, 여기서는 회원의 고유번호를 입력한다.
 * @param loginPassword 회원탈퇴 체크를 위한 회원의 로그인 비밀번호를 입력한다.
 */
data class SecessionRequest(
    val userId: Int,
    val loginPassword: String?,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val userType: Int = UserType.MEMBER.id,
)