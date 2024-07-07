package com.theone.busandbt.dto.payment.request

data class SendOrderDongBaekGeonRequest(
    val orderId: String,
    val showMenuName: String,
    val paymentCost: Int,
    val shopId: Int
)