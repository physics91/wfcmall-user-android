package com.theone.busandbt.dto.cost

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeliveryCost(
    val testOrderCost: Int,
    val cost: Int
) : Parcelable