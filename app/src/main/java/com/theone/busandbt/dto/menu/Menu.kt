package com.theone.busandbt.dto.menu

import android.os.Parcelable
import com.theone.busandbt.dto.cost.MenuCostListItem
import com.busandbt.code.MenuStatus
import kotlinx.parcelize.Parcelize

/**
 * @param status [MenuStatus]
 */
@Parcelize
data class Menu(
    val id: Int,
    val status: Int,
    val name: String,
    val struct: String,
    val desc: String,
    val count: Int,
    val remainCount: Int,
    val visibleIndex: Int,
    val isMainMenu: Boolean,
    val isAdultMenu: Boolean,
    val likeCount: Int,
    val imageUrl: String,
    val optionGroupList: List<MenuOptionGroup>,
    val menuCostList: List<MenuCostListItem>
) : Parcelable {

    /**
     * 해당 상품을 구매가능한 상태인지 판단한다.
     */
    val available get() = remainCount > 0 && status == MenuStatus.SELLING.id
}