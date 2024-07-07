package com.theone.busandbt.dto.menu

import com.theone.busandbt.dto.cost.MenuCostListItem

data class MenuBasketInfo(
    val menuId: Int,
    val menuCostList: List<MenuCostListItem>
)