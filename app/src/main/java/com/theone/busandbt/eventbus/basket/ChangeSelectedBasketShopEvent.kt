package com.theone.busandbt.eventbus.basket

import com.theone.busandbt.item.basket.BasketShop

data class ChangeSelectedBasketShopEvent(
    val old: BasketShop?,
    val basketShop: BasketShop
)