package com.theone.busandbt.api.orderchannel

import com.busandbt.code.ChannelType
import com.busandbt.code.ServiceType
import com.theone.busandbt.dto.category.CategoryListItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 카테고리 API
 */
interface CategoryAPI {

    /**
     * 서비스 유형 별 카테고리 리스트 조회
     * @param serviceTypeList 서비스 유형 리스트 [ServiceType]
     */
    @GET("/category/v2")
    fun getCategoryList(
        @Query("serviceTypeList") serviceTypeList: List<Int>,
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id
    ): Call<List<CategoryListItem>>
}