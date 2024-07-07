package com.theone.busandbt.dto.coupon

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UseCoupon(
    val shopId: Int,
    val couponId: Int,
    val couponName: String,
    val couponDiscountCost: Int,
    val type: Int?
) : Parcelable