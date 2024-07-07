package com.theone.busandbt.api.orderchannel

import com.theone.busandbt.dto.search.GetSearchAllListResponse
import com.busandbt.code.ChannelType
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchAPI {

    /**
     * 전체 검색
     */
    @GET("/search")
    fun getSearchAllList(
        @Query("jibun") jibun: String,
        @Query("keyword") keyword: String,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<GetSearchAllListResponse>
}