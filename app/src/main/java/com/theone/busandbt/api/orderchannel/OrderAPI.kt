package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.order.*
import com.busandbt.code.ChannelType
import com.busandbt.code.OrderStatus
import com.theone.busandbt.dto.order.request.AddOrderRequest
import com.theone.busandbt.dto.order.request.SetOrderCancelRequest
import com.theone.busandbt.dto.order.response.AddOrderResponse
import com.theone.busandbt.dto.order.response.CheckCanOrderResponse
import com.theone.busandbt.dto.order.response.CreateOrderIdResponse
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import retrofit2.Call
import retrofit2.http.*

/**
 * 주문 관련 API들을 정의한다. (주문채널)
 */
interface OrderAPI {

    /**
     * 회원 주문 현황 리스트 조회 (쇼핑몰 제외)
     * @param statusList 주문 상태 필터 [OrderStatus] 1,2,3 형식으로 입력할 것
     */
    @GET("/order/v2")
    fun getOrderList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
        @Query("serviceType") serviceType: Int? = null,
        @Query("deliveryType") deliveryType: Int? = null,
        @Query("memberId") memberId: Int? = null,
        @Query("orderStatusList", encoded = true) statusList: String? = null
    ): Call<List<OrderListItem>>

    /**
     * 회원 주문 현황 리스트 조회 (쇼핑몰)
     */
    @GET("/order/v2/mall")
    fun getMallOrderList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("count") count: Int,
        @Query("memberId") memberId: Int
    ): Call<List<MallOrderListItem>>

    /**
     * 주문이 가능한 상태인지 체크한다.
     * @param request 주문하기 API와 같은 데이터로 요청해야한다.
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/order/v2/check")
    fun checkCanOrder(
        @Header("Authorization") token: String,
        @Body request: AddOrderRequest
    ): Call<CheckCanOrderResponse>

    /**
     * 주문 상세 조회 (쇼핑몰 제외)
     */
    @GET("/order/v2/{orderId}")
    fun getOrderDetail(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Call<OrderDetail>

    /**
     * 주문 상세 조회 (쇼핑몰)
     */
    @GET("/order/v2/mall/{orderId}")
    fun getMallOrderDetail(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Call<MallOrderDetail>

    /**
     * 주문 현황 조회 (쇼핑몰 제외)
     */
    @GET("/order/v2/status/{orderId}")
    fun getOrderStatusInfo(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Call<OrderStatusInfo>

    /**
     * 주문 요청
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/order/v2")
    fun addOrder(
        @Header("Authorization") token: String,
        @Body request: AddOrderRequest
    ): Call<AddOrderResponse>

    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @PUT("/order/v2/{orderId}")
    fun setOrderCancel(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String,
        @Body request: SetOrderCancelRequest
    ): Call<Unit>

    @DELETE("/order/v2/{orderId}")
    fun removeOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Call<Unit>

    /**
     * 주문 번호 생성 API
     */
    @GET("/order/id")
    fun createOrderId(
        @Header("Authorization") token: String,
        @Query("appType") appType: Int,
        @Query("serviceType") serviceType: Int,
        @Query("deliveryType") deliveryType: Int
    ): Call<CreateOrderIdResponse>
}