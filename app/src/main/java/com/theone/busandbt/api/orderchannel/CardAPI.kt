package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.card.CardInfoListItem
import com.theone.busandbt.dto.card.GetCardTypeResponse
import com.theone.busandbt.dto.card.request.AddCardInfoRequest
import com.theone.busandbt.dto.card.response.AddCardInfoResponse
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import retrofit2.Call
import retrofit2.http.*

interface CardAPI {

    @DELETE("/card/{cardInfoId}")
    fun removeCard(
        @Header("Authorization") token: String,
        @Path("cardInfoId") cardInfoId: Int
    ): Call<Unit>

    @GET("/card")
    fun getCardList(
        @Header("Authorization") token: String,
        @Query("memberId") memberId: Int,
    ): Call<List<CardInfoListItem>>

    @GET("/card/type")
    fun getCardType(
        @Query("cardNo") cardNo: String,
    ): Call<GetCardTypeResponse>

    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/card")
    fun cardRegistration(
        @Header("Authorization") token: String,
        @Body request: AddCardInfoRequest
    ): Call<AddCardInfoResponse>
}