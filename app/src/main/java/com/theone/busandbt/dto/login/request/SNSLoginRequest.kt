package com.theone.busandbt.dto.login.request

import com.busandbt.code.ChannelType
import com.busandbt.code.Gender
import com.busandbt.code.JoinType
import com.busandbt.code.LoginDeviceType
import com.busandbt.code.UserType
import com.theone.busandbt.BuildConfig
import java.time.LocalDate

/**
 * SNS 로그인 요청 데이터
 * 연동 로그인 시에 가입된 계정이 없다면 이 요청으로 자동 회원가입이 진행된다.
 * @param gender 성별, [Gender] 성별을 얻을 수 없는 경우 [Gender.MAN]으로 입력한다.
 * @param joinType 가입경로, 예시)카톡, 네이버, 구글 등 [JoinType]
 * @param snsClientId 각 연동 로그인 계정의 고유값을 입력한다. 연동 로그인 결과값에 포함되어 있다.
 */
data class SNSLoginRequest(
    val name: String,
    val nickname: String,
    val tel: String,
    val birth: LocalDate,
    val gender: Int,
    val joinType: Int,
    val snsClientId: String,
    val email: String?,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val userType: Int = UserType.MEMBER.id,
    val loginDeviceType: LoginDeviceType = LoginDeviceType.ANDROID,
    val version: String = BuildConfig.VERSION_NAME,
)