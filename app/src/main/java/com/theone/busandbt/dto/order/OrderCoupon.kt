package com.theone.busandbt.dto.order

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderCoupon(
    val couponName: String,
    val couponDiscountCost: Int
) : Parcelable