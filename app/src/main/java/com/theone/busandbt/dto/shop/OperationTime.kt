package com.theone.busandbt.dto.shop

import com.busandbt.code.OperTimeType

/**
 * @param type 운영시간 유형 [OperTimeType]
 */
data class OperationTime(
    val id: Int,
    val shopId: Int,
    val type: Int,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val createDateTime: String
)