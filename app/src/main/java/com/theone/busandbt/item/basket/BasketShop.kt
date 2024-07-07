package com.theone.busandbt.item.basket

import com.theone.busandbt.dto.menu.MenuOptionGroup

data class BasketShop(
    var id: Long,
    val shopId: Int,
    val shopName: String,
    val serviceTypeId: Int,
    var deliveryTypeId: Int,
    var deliveryCost: Int,
    val minOrderCost: Int,
    var isSelected: Boolean,
    val menuList: MutableList<BasketMenu>
) {
    companion object {
        fun createForAdd(
            shopId: Int,
            shopName: String,
            serviceTypeId: Int,
            deliveryTypeId: Int,
            deliveryCost: Int,
            minOrderCost: Int,
            menuId: Int,
            menuStatus: Int,
            menuName: String,
            isAdultMenu: Boolean,
            menuCost: Int,
            menuSaleCost: Int,
            menuCount: Int,
            menuImageUrl: String,
            menuOptionGroupList: List<MenuOptionGroup>,
            menuCostId: Int,
            menuCostName: String,
        ): BasketShop {
            val basketMenuOptionList = menuOptionGroupList.flatMap { optionGroup ->
                optionGroup.optionList.map { option ->
                    BasketMenuOption.createForAdd(
                        menuId,
                        option.id,
                        optionGroup.name,
                        option.name,
                        option.status,
                        option.cost
                    )
                }
            }
            val basketMenu = BasketMenu.createForAdd(
                menuId,
                menuStatus,
                menuName,
                basketMenuOptionList,
                isAdultMenu,
                menuImageUrl,
                menuCost,
                menuSaleCost,
                menuCount,
                menuCostId,
                menuCostName
            )
            return BasketShop(
                0,
                shopId,
                shopName,
                serviceTypeId,
                deliveryTypeId,
                deliveryCost,
                minOrderCost,
                true,
                arrayListOf(basketMenu)
            )
        }
    }
}