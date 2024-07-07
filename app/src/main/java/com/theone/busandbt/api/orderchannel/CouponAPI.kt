package com.theone.busandbt.api.orderchannel

import com.busandbt.code.ChannelType
import com.theone.busandbt.dto.coupon.MemberCouponByShop
import com.theone.busandbt.dto.coupon.MemberCouponListItem
import com.theone.busandbt.dto.coupon.ShopCouponListItem
import com.theone.busandbt.dto.coupon.request.DownloadCouponListRequest
import com.theone.busandbt.dto.coupon.request.IssueCouponByCodeRequest
import com.theone.busandbt.dto.coupon.request.IssueCouponRequest
import com.theone.busandbt.dto.coupon.response.GetMemberCouponCountResponse
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import retrofit2.Call
import retrofit2.http.*

/**
 * 쿠폰 API
 */
interface CouponAPI {

    /**
     * 상점이 발행하고 있는 쿠폰 리스트 조회
     * @param shopId 대상 상점의 고유번호
     */
    @GET("/coupon/v2/downloadable/fixed")
    fun getShopCouponList(
        @Query("shopId") shopId: Int,
        @Query("memberId") memberId: Int?
    ): Call<List<ShopCouponListItem>>

    /**
     * 회원이 소유한 쿠폰 리스트 조회
     * @param page 리스트의 페이지를 입력한다. 1이라면 첫 번째 데이터부터 [count]개를 조회한다.
     * @param count 페이지 당 조회할 데이터 개수를 입력한다.
     * @param memberId 대상 회원의 고유번호
     */
    @GET("/aw/coupon/member/plus")
    fun getMemberCouponByShopList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("memberId") memberId: Int,
        @Query("serviceType") serviceType: Int? = null,
        @Query("deliveryType") deliveryType: Int? = null,
        @Query("couponType") couponType: Int? = null,
        @Query("shopId") shopId: Int? = null,
        @Query("shopIdList") shopIdList: List<Int>? = null,
        @Query("paymentCost") paymentCost: Int? = null,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("otherCouponIdList ") otherCouponIdList : List<Int>? = null
    ): Call<List<MemberCouponByShop>>

    /**
     * 회원이 소유한 쿠폰 리스트 조회
     * @param page 리스트의 페이지를 입력한다. 1이라면 첫 번째 데이터부터 [count]개를 조회한다.
     * @param count 페이지 당 조회할 데이터 개수를 입력한다.
     * @param memberId 대상 회원의 고유번호
     */
    @GET("/aw/coupon/member/each")
    fun getMemberCouponList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("memberId") memberId: Int,
        @Query("serviceType") serviceType: Int? = null,
        @Query("deliveryType") deliveryType: Int? = null,
        @Query("couponType") couponType: Int? = null,
        @Query("shopId") shopId: Int? = null,
        @Query("shopIdList") shopIdList: List<Int>? = null,
        @Query("paymentCost") paymentCost: Int? = null,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<List<MemberCouponListItem>>

    @GET("/coupon/v2/member/count/{memberId}")
    fun getMemberCouponCount(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: Int,
        @Query("serviceType") serviceType: Int? = null,
        @Query("deliveryType") deliveryType: Int? = null,
        @Query("couponType") couponType: Int? = null,
        @Query("shopId") shopId: Int? = null,
        @Query("paymentCost") paymentCost: Int? = null,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<GetMemberCouponCountResponse>

    /**
     * 회원 쿠폰 여러장 다운로드
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/coupon/v2/download")
    fun downloadCouponList(
        @Header("Authorization") token: String,
        @Body request: DownloadCouponListRequest
    ): Call<Unit>

    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/aw/coupon/issue")
    fun issueCoupon(
        @Header("Authorization") token: String,
        @Body request: IssueCouponRequest
    ): Call<Unit>

    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/aw/coupon/issue/code")
    fun issueCouponByCode(
        @Header("Authorization") token: String,
        @Body request: IssueCouponByCodeRequest
    ): Call<Unit>
}