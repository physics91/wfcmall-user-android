package com.theone.busandbt.dto.menu

import com.theone.busandbt.dto.cost.MenuCostListItem
import com.theone.busandbt.dto.shop.RefundInfo
import com.busandbt.code.MenuStatus

data class MallMenuDetail(
    val id: Int,
    val status: Int,
    val name: String,
    val struct: String,
    val desc: String,
    val remainCount: Int,
    val isAdultMenu: Boolean,
    val maxDiscountCost: Int,
    val deliveryCost: Int,
    val paymentTypeNameList: List<String>,
    val orderCostForFree: Int,
    val shopId: Int,
    val shopName: String,
    val imageUrlList: List<String>,
    val menuIntroImageUrlList: List<String>,
    val menuInfoList: List<MenuInfo>?,
    val reviewCount: Int,
    val optionGroupList: List<MenuOptionGroup>,
    val menuCostList: List<MenuCostListItem>,
    val refundInfoList: List<RefundInfo>?,
) {
    /**
     * 해당 상품을 구매가능한 상태인지 판단한다.
     */
    val available get() = remainCount > 0 && status == MenuStatus.SELLING.id
}