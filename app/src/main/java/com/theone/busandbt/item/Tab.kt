package com.theone.busandbt.item

import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType

data class Tab(
    val serviceType: ServiceType,
    val deliveryType: DeliveryType
)