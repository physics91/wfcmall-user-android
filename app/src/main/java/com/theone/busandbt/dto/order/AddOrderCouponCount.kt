package com.theone.busandbt.dto.order

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddOrderCouponCount(
    val shopCouponCount: Int,
    val eventCouponCount: Int
) : Parcelable