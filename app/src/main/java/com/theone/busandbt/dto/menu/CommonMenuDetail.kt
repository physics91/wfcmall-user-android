package com.theone.busandbt.dto.menu

import com.busandbt.code.MenuStatus

data class CommonMenuDetail(
    val menuId: Int,
    val menuName: String,
    val menuDesc: String,
    val menuCost: Int,
    val menuSaleCost: Int,
    val reviewCount: Int,
    val imageUrlList: List<String>,
    val optionGroupList: List<MenuOptionGroup>,
    val remainCount: Int,
    val menuStatus: MenuStatus,
    val available: Boolean,
    val isAdultMenu: Boolean
)