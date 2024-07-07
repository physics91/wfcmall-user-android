package com.theone.busandbt.dto.login

/**
 * 사용자 아이디 중복 체크 응답 객체
 * @param testResult true면 이미 존재하는 아이디라는 뜻
 * @param userId 사용자 고유번호 (=memberId), [testResult]가 true일 경우에만 값이 넘어온다.
 */
data class CheckLoginIdResponse(
    val testResult: Boolean,
    val userId: Int?
)