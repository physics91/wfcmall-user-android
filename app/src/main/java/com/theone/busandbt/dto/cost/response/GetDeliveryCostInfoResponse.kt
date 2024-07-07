package com.theone.busandbt.dto.cost.response

import com.theone.busandbt.dto.cost.DeliveryCost

/**
 * 할증 요금 조회 응답 데이터
 */
data class GetDeliveryCostInfoResponse(
    val deliveryCostList: List<DeliveryCost>,
    val areaExtraCost: Int,
    val distanceExtraCost: Int,
    val holidayExtraCost: Int,
    val dongExtraCost: Int,
    val timeExtraCost: Int
)