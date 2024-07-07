package com.theone.busandbt.dto.order.request

data class SetParcelInfo(
    val sequence: Int,
    val parcelStatus: Int? = null,
    val courierType: Int? = null,
    val waybillNo: String? = null,
    val parcelType: Int? = null,
    val returnCost: Int? = null,
    val cancelType: Int? = null,
    val cancelReason: String? = null
)