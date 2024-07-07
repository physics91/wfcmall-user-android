package com.theone.busandbt.dto.payment.response

data class SendOrderDongBaekGeonResponse(
    val trNo: String,
    val secret: String,
    val frchNo: String
)