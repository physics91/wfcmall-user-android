package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.review.MemberReviewListItem
import com.theone.busandbt.dto.review.ReviewCount
import com.theone.busandbt.dto.review.ReviewListItem
import com.theone.busandbt.dto.review.ReviewStatistics
import com.theone.busandbt.dto.review.request.AddReviewRequest
import com.theone.busandbt.dto.review.request.SetReviewRequest
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import com.busandbt.code.ChannelType
import retrofit2.Call
import retrofit2.http.*

/**
 * 리뷰 API
 */
interface ReviewAPI {

    /**
     * 리뷰 리스트 조회
     * @param page 리스트의 페이지를 입력한다. 1이라면 첫 번째 데이터부터 [count]개를 조회한다.
     * @param count 페이지 당 조회할 데이터 개수를 입력한다.
     */
    @GET("/review/v2")
    fun getReviewList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("shopId") shopId: Int,
        @Query("memberId") memberId: Int? = null,
        @Query("reviewSortType") reviewSortType: Int? = null,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
    ): Call<List<ReviewListItem>>

    /**
     * 회원 리뷰 리스트 조회
     * @param page 리스트의 페이지를 입력한다. 1이라면 첫 번째 데이터부터 [count]개를 조회한다.
     * @param count 페이지 당 조회할 데이터 개수를 입력한다.
     */
    @GET("/review/v2/member/plus")
    fun getMemberReviewList(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("memberId") memberId: Int,
        @Query("serviceType") serviceType: Int,
        @Query("deliveryType") deliveryType: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
    ): Call<List<MemberReviewListItem>>

    /**
     * 메뉴별 리뷰 리스트 조회
     * @param page 리스트의 페이지를 입력한다. 1이라면 첫 번째 데이터부터 [count]개를 조회한다.
     * @param count 페이지 당 조회할 데이터 개수를 입력한다.
     * @param menuId 메뉴 데이터의 고유번호
     */
    @GET("/review/v2/menu")
    fun getReviewListByMenu(
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("menuId") menuId: Int,
        @Query("reviewSortType") reviewSortType: Int,
        @Query("memberId") memberId: Int? = null,
    ): Call<List<ReviewListItem>>

    /**
     * 메뉴별 통계 정보 조회
     */
    @GET("/review/statistics/{shopId}")
    fun getReviewStatistics(
        @Path("shopId") shopId: Int
    ): Call<ReviewStatistics>

    @GET("/review/count/menu/{menuId}")
    fun getReviewCountByMenu(
        @Path("menuId") menuId: Int
    ): Call<ReviewCount>

    /**
     * 리뷰 등록
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/review")
    fun addReview(
        @Header("Authorization") token: String,
        @Body request: AddReviewRequest
    ): Call<Unit>

    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @PUT("/review/{reviewId}")
    fun setReview(
        @Header("Authorization") token: String,
        @Path("reviewId") reviewId: Int,
        @Body request: SetReviewRequest
    ): Call<Unit>

    @DELETE("/review/{reviewId}")
    fun removeReview(
        @Header("Authorization") token: String,
        @Path("reviewId") reviewId: Int
    ): Call<Unit>
}