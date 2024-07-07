package com.theone.busandbt.dto.payment.request

data class ApprovalDongBaekGeonRequest(
    val userId: String,
    val trNo: String,
    val secret: String,
    val shopId: Int,
    val orderId: String
)