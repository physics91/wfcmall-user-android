package com.theone.busandbt.dto.menu

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewAndPopularMallMenuListItem(
    val id: Int,
    val name: String,
    val status: Int,
    val shopId: Int,
    val shopName: String,
    val imageUrl: String,
    val cost: Int,
    val saleCost: Int,
    val discountRate: Int,
    val isSafeFood: Boolean,
    val hasCoupon: Boolean
) : Parcelable, java.io.Serializable