package com.theone.busandbt.dto.promotion

import com.busandbt.code.PromotionStatus
import com.busandbt.code.PromotionType
import com.busandbt.code.ServiceType

/**
 * 프로모션 상세 데이터
 * @param serviceType 서비스 유형 [ServiceType]
 * @param type 프로모션 유형 [PromotionType]
 * @param status 프로모션 상태 [PromotionStatus]
 */
data class PromotionDetail(
    val serviceType: Int,
    val type: Int,
    val status: Int,
    val visibleIndex: Int,
    val startDateTime: String?,
    val endDateTime: String?,
    val title: String,
    val imageUrl: String,
    val landingUrl: String,
    val clickCount: Int,
    val createDateTime: String
)