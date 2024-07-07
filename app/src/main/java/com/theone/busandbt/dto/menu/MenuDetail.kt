package com.theone.busandbt.dto.menu

import com.theone.busandbt.dto.cost.MenuCostListItem
import com.busandbt.code.MenuStatus

/**
 * 메뉴 상세 정보
 */
data class MenuDetail(
    val id: Int,
    val status: Int,
    val name: String,
    val struct: String,
    val desc: String,
    val cost: Int,
    val count: Int,
    val remainCount: Int,
    val isAdultMenu: Boolean,
    val imageUrlList: List<String>,
    val reviewCount: Int,
    val optionGroupList: List<MenuOptionGroup>,
    val menuCostList: List<MenuCostListItem>?
) {
    /**
     * 해당 상품을 구매가능한 상태인지 판단한다.
     */
    val available get() = remainCount > 0 && status == MenuStatus.SELLING.id
}