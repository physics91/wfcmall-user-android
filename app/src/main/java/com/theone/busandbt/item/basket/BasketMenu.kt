package com.theone.busandbt.item.basket

/**
 * @param basketShopId 상점 장바구니 데이터의 고유번호 = [BasketShop.id]
 */
data class BasketMenu(
    var id: Long,
    var basketShopId: Long,
    val menuId: Int,
    var status: Int,
    val name: String,
    val optionList: MutableList<BasketMenuOption>,
    val isAdultMenu: Boolean,
    val imageUrl: String,
    var cost: Int,
    var saleCost: Int,
    var isSelected: Boolean,
    var count: Int,
    val menuCostId: Int,
    val menuCostName: String
) {
    companion object {
        fun createForAdd(
            menuId: Int,
            status: Int,
            name: String,
            optionList: List<BasketMenuOption>,
            isAdultMenu: Boolean,
            imageUrl: String,
            cost: Int,
            saleCost: Int,
            count: Int,
            menuCostId: Int,
            menuCostName: String
        ) = BasketMenu(
            0,
            0,
            menuId,
            status,
            name,
            optionList.toMutableList(),
            isAdultMenu,
            imageUrl,
            cost,
            saleCost,
            true,
            count,
            menuCostId,
            menuCostName
        )
    }

    fun isContentSame(basketMenu: BasketMenu): Boolean {
        if (menuId != basketMenu.menuId) return false
        if (menuCostId != basketMenu.menuCostId) return false
        return optionList.map { it.optionId }.toString() == basketMenu.optionList.map { it.optionId }
            .toString()
    }
}