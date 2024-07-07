package com.theone.busandbt.api.orderchannel

import com.busandbt.code.ChannelType
import com.busandbt.code.PromotionStatus
import com.busandbt.code.PromotionType
import com.busandbt.code.ServiceType
import com.theone.busandbt.dto.promotion.PromotionDetail
import com.theone.busandbt.dto.promotion.PromotionListItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 프로모션 API (이벤트, 광고 등을 의미한다.)
 */
interface PromotionAPI {

    /**
     * 프로모션 리스트 조회
     * @param serviceType 서비스 유형으로 검색할 수 있다. [ServiceType]
     * @param promotionType 프로모션 유형으로 검색할 수 있다. 팝업 혹은 배너 [PromotionType]
     * @param promotionStatus 프로모션 상태로 검색할 수 있다. 여기서는 무조건 PROGRESS로 입력한다.
     */
    @GET("/aw/promotion")
    fun getPromotionList(
        @Query("page") page: Int = 1,
        @Query("count") count: Int = 15,
        @Query("promotionType") promotionType: Int? = null,
        @Query("promotionStatus") promotionStatus: Int = PromotionStatus.PROGRESS.id,
        @Query("displayLocation") displayLocation: Int? = null,
    ): Call<List<PromotionListItem>>

    /**
     * 프로모션 상세 조회
     * @param promotionId 프로모션 고유번호
     */
    @GET("/promotion/{promotionId}")
    fun getPromotionDetail(
        @Path("promotionId") promotionId: Int
    ): Call<PromotionDetail>

    /**
     * 랜덤 프로모션 팝업 데이터 조회
     */
    @GET("/promotion/popup/random")
    fun getRandomPromotionPopup(
        @Query("channelType") channelType: Int = ChannelType.DONG_BAEK.id,
    ): Call<PromotionDetail>
}