package com.theone.busandbt.dto.order

data class MallOrderListItem(
    val id: String,
    val menuName: String,
    val paymentCost: Int,
    val createDateTime: String
)