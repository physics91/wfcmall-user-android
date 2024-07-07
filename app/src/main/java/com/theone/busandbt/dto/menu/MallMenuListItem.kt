package com.theone.busandbt.dto.menu

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MallMenuListItem(
    val id: Int,
    val name: String,
    val status: Int,
    val cost: Int,
    val saleCost: Int,
    val discountRate: Int,
    val imageUrl: String,
    val shopId: Int,
    val shopName: String,
    val hasCoupon: Boolean,
    val isSafeFood: Boolean
) : Parcelable