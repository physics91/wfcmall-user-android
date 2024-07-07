package com.theone.busandbt.dto.shop

import android.os.Parcelable
import com.busandbt.code.DeliveryType
import kotlinx.parcelize.Parcelize

/**
 * @param deliveryTypeList 배달유형 리스트 [DeliveryType]
 */
@Parcelize
data class DiscountShopListItem(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val deliveryTypeList: List<Int>,
    val minOrderCost: Int,
    val star: Double,
    val orderDoneMinutes: Int,
    val minDeliveryCost: Int,
    val maxDeliveryCost: Int,
    val maxDiscountCost: Int,
) : Parcelable