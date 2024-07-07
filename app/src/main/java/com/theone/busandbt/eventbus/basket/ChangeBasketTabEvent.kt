package com.theone.busandbt.eventbus.basket

import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.item.basket.BasketShop

data class ChangeBasketTabEvent(
    val serviceType: ServiceType,
    val deliveryType: DeliveryType,
    val basketShop: BasketShop? = null
)