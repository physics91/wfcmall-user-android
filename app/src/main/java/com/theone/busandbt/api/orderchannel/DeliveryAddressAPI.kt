package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.address.AddDeliveryAddressIdResponse
import com.theone.busandbt.dto.address.ServerDeliveryAddress
import com.theone.busandbt.dto.address.request.AddDeliveryAddressRequest
import com.theone.busandbt.dto.address.request.SetDeliveryAddressRequest
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import retrofit2.Call
import retrofit2.http.*

/**
 * 회원의 배달주소 관련 API들을 정의한다. (주문채널)
 */
interface DeliveryAddressAPI {

    /**
     * 회원의 배달주소 리스트 조회
     */
    @GET("/delivery/address")
    fun getDeliveryAddressList(
        @Header("Authorization") token: String,
        @Query("memberId") memberId: Int
    ): Call<List<ServerDeliveryAddress>>

    /**
     * 배달주소 등록
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/delivery/address")
    fun addDeliveryAddress(
        @Header("Authorization") token: String,
        @Body request: AddDeliveryAddressRequest
    ): Call<AddDeliveryAddressIdResponse>

    /**
     * 배달주소 수정
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @PUT("/delivery/address/{deliveryAddressId}")
    fun setDeliveryAddress(
        @Header("Authorization") token: String,
        @Path("deliveryAddressId") deliveryAddressId: Int,
        @Body request: SetDeliveryAddressRequest
    ): Call<Unit>

    /**
     * 배달주소 삭제
     */
    @DELETE("/delivery/address/{deliveryAddressId}")
    fun removeDeliveryAddress(
        @Header("Authorization") token: String,
        @Path("deliveryAddressId") deliveryAddressId: Int
    ): Call<Unit>
}