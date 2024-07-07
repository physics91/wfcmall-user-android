package com.theone.busandbt.dto.order

data class OrderListItem(
    val id: String,
    var status: Int,
    val shopId: Int,
    val shopName: String,
    val shopImageUrl: String,
    val paymentCost: Int,
    var doWrittenReview: Boolean,
    val createDateTime: String,
    val orderDoneDateTime: String?
)