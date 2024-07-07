package com.theone.busandbt.eventbus

import com.busandbt.code.OrderStatus

data class ChangeOrderStatusEvent(
    val orderId: String,
    val oldOrderStatus: OrderStatus,
    val orderStatus: OrderStatus,
    val orderDoneMinutes: Int?
)