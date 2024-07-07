package com.theone.busandbt.dto.order.request

import com.busandbt.code.CancelType
import com.busandbt.code.ChannelType
import com.busandbt.code.OrderStatus

data class SetOrderCancelRequest(
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val status: Int? = OrderStatus.CANCEL.id,
    val cancelType: Int? = CancelType.CUSTOMER_REQUEST.id,
    val cancelReason: String? = null,
    val setParcelInfoList: List<SetParcelInfo>? = null
)