package com.theone.busandbt.dto.cert.request

/**
 * 휴대폰 인증번호 발급/재발급 요청 데이터
 * @param tel 인증 대상의 휴대폰 번호
 */
data class AddCertNumberRequest(
    val tel: String
)