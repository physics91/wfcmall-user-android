package com.theone.busandbt.dto.order

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 주문상세에 포함되는 주문 메뉴 데이터
 */
@Parcelize
data class OrderMenuInDetail(
    val menuId: Int,
    val menuName: String,
    val optionList: List<OrderMenuOption>,
    val count: Int,
    val cost: Int,
    val menuImageUrl: String,
    val menuCostName: String
) : Parcelable