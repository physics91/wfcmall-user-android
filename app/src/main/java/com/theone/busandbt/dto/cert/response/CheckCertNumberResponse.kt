package com.theone.busandbt.dto.cert.response

/**
 * 휴대폰 인증 응답 데이터
 * @param testResult true일 경우 인증번호로 인증이 완료됐다는 뜻
 * @param token 강제 비밀번호 변경을 하기 위한 토큰, testResult가 true일 경우에만 null이 아니다.
 */
data class CheckCertNumberResponse(
    val testResult: Boolean,
    val token: String?
)