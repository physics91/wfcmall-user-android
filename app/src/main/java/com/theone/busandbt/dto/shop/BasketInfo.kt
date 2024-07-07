package com.theone.busandbt.dto.shop

import android.os.Parcelable
import com.theone.busandbt.dto.cost.DeliveryCost
import kotlinx.parcelize.Parcelize

@Parcelize
data class BasketInfo(
    val shopId: Int,
    val deliveryTypeList: List<Int>,
    val deliveryCostList: List<DeliveryCost>,
    val paymentTypeList: List<Int>,
    val extraCost: Int,
    val minOrderCost: Int
): Parcelable