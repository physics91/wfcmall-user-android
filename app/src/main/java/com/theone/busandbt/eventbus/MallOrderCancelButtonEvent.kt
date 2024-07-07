package com.theone.busandbt.eventbus

import com.theone.busandbt.databinding.ItemShoppingOrderDetailBinding
import com.theone.busandbt.dto.order.MallOrderShopInDetail

data class MallOrderCancelButtonEvent(
    val token: String,
    val orderId: String,
    val item: MallOrderShopInDetail,
    val binding: ItemShoppingOrderDetailBinding
)