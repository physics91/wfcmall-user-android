package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType
import com.busandbt.code.LoginDeviceType
import com.busandbt.code.UserType
import com.theone.busandbt.BuildConfig

/**
 * 일반 로그인 요청 데이터
 * @param loginId 아이디 입력 규칙 확인해서 입력할 것
 * @param loginPassword 비밀번호 입력 규칙 확인해서 입력할 것
 */
data class NormalLoginRequest(
    val loginId: String,
    val loginPassword: String,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val userType: Int = UserType.MEMBER.id,
    val loginDeviceType: LoginDeviceType = LoginDeviceType.ANDROID,
    val version: String = BuildConfig.VERSION_NAME,
)