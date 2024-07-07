package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.member.response.CheckSnsMemberResponse
import com.theone.busandbt.dto.member.MemberDetail
import com.theone.busandbt.dto.member.request.CheckSnsMemberRequest
import com.theone.busandbt.dto.member.request.SetMemberRequest
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import retrofit2.Call
import retrofit2.http.*

/**
 * 회원 API
 */
interface MemberAPI {

    /**
     * 회원정보 조회
     */
    @GET("/member/{memberId}")
    fun getMemberDetail(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: Int
    ): Call<MemberDetail>

    /**
     * SNS 회원이 존재하는지 체크한다.
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/member/sns/check")
    fun checkSnsMember(
        @Body request: CheckSnsMemberRequest
    ): Call<CheckSnsMemberResponse>

    /**
     * 회원정보 수정
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @PUT("/member/{memberId}")
    fun setMember(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: Int,
        @Body request: SetMemberRequest
    ): Call<Unit>

    /**
     * 현재 주문이 진행중인 서비스 유형 조회 (쇼핑몰은 제외)
     */
    @GET("/member/order/process")
    fun getOrderProcessServiceTypeList(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: Int
    ): Call<List<Int>>
}