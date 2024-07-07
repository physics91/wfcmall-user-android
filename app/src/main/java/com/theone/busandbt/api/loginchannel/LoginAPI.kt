package com.theone.busandbt.api.loginchannel

import com.theone.busandbt.dto.login.CheckLoginIdResponse
import com.theone.busandbt.dto.login.LoginSuccessResponse
import com.theone.busandbt.dto.login.request.*
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import retrofit2.Call
import retrofit2.http.*

/**
 * 로그인 API
 */
interface LoginAPI {

    /**
     * 일반 로그인
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/login")
    fun login(@Body request: NormalLoginRequest): Call<LoginSuccessResponse>

    /**
     * 토큰 로그인 (자동 로그인에 사용)
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/login/token")
    fun loginByToken(
        @Header("Authorization") token: String,
        @Body request: LoginByTokenRequest
    ): Call<LoginSuccessResponse>

    /**
     * SNS 연동 로그인
     * 회원가입도 동시에 진행된다.
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/login/sns/member")
    fun loginBySNS(@Body request: SNSLoginRequest): Call<LoginSuccessResponse>

    /**
     * 로그아웃
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/logout")
    fun logout(
        @Header("Authorization") token: String,
        @Body request: LogoutRequest
    ): Call<Unit>

    /**
     * 일반 회원가입
     * 회원가입과 동시에 로그인도 진행된다.
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/join")
    fun join(@Body request: JoinRequest): Call<LoginSuccessResponse>

    /**
     * 계정 비밀번호 변경
     * @param userId 비밀번호 변경 대상, 여기서는 memberId를 입력한다.
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @PUT("/user/password/{userId}")
    fun setPassword(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: SetPasswordRequest
    ): Call<Unit>

    /**
     * 강제 계정 비밀번호 변경
     * 비밀번호 찾기(재설정) 화면에서 비밀번호 변경할때 사용한다.
     * @param tempToken CertAPI에서의 인증 번호 체크 결과로 받은 임시 토큰을 입력한다. (일반 로그인 토큰과는 다르다.)
     * @param userId 비밀번호 변경 대상, 여기서는 memberId를 입력한다.
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @PUT("/user/password/{userId}/forced")
    fun setPasswordForced(
        @Header("Authorization") tempToken: String,
        @Path("userId") userId: Int,
        @Body request: SetPasswordForcedRequest
    ): Call<Unit>

    /**
     * 회원탈퇴
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/user/secession")
    fun secession(
        @Header("Authorization") token: String,
        @Body request: SecessionRequest
    ): Call<Unit>

    /**
     * 사용자 계정 중복 체크
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/user/id/check")
    fun checkLoginId(
        @Body request: CheckLoginIdRequest
    ): Call<CheckLoginIdResponse>
}