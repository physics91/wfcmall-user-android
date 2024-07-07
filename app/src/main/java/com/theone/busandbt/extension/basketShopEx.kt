package com.theone.busandbt.extension

import com.theone.busandbt.item.basket.BasketShop

fun BasketShop.getSelectedMenuList() = menuList.filter { it.isSelected }