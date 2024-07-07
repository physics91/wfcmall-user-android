package com.theone.busandbt.dto.order

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 주문 메뉴 옵션 객체
 */
@Parcelize
data class OrderMenuOption(
    val id: Int,
    val name: String,
    val groupName: String,
    val cost: Int
) : Parcelable