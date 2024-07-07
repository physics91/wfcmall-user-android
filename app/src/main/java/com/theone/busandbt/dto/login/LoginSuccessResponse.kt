package com.theone.busandbt.dto.login

/**
 * 로그인 성공 응답 데이터
 * 자동로그인 때 사용하기 위해 클라이언트 캐시로 저장해두어야한다.
 * @param userId 사용자 고유번호, 상점, 회원 모두 포함되지만 여기선 회원 고유번호 (memberId)
 * @param token 사용자 API를 사용하기 위한 보안토큰, 자동로그인에서도 사용한다.
 */
data class LoginSuccessResponse(
    val userId: Int,
    val token: String
)
