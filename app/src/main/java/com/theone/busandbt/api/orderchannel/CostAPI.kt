package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.cost.response.GetDeliveryCostInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 배달료, 할증 등 비용 관련 API
 */
interface CostAPI {

    /**
     * 상점의 배달료 정보 조회
     * @param shopId 할증 대상 상점 고유번호
     */
    @GET("/cost/delivery/info/{shopId}")
    fun getDeliveryCostInfo(
        @Path("shopId") shopId: Int,
        @Query("jibun") jibun: String,
        @Query("customerLat") customerLat: Double,
        @Query("customerLng") customerLng: Double,
    ): Call<GetDeliveryCostInfoResponse>
}