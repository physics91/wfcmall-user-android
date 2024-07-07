package com.theone.busandbt.dto.cert.request

import com.busandbt.code.ChannelType

/**
 * 휴대폰 인증번호 요청 데이터
 * @param tel 인증 휴대폰 번호
 * @param certNumber 입력한 인증번호
 */
data class CheckCertNumberRequest(
    val tel: String,
    val certNumber: String,
    val channelType: Int = ChannelType.DONG_BAEK.id
)