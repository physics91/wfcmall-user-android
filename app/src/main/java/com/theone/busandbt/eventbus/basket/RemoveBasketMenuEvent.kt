package com.theone.busandbt.eventbus.basket

import com.theone.busandbt.item.basket.BasketMenu
import com.theone.busandbt.item.basket.BasketShop

data class RemoveBasketMenuEvent(
    val basketShop: BasketShop,
    val basketMenu: BasketMenu
)