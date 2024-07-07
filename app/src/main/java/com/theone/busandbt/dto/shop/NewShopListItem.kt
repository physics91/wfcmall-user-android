package com.theone.busandbt.dto.shop

import android.os.Parcelable
import com.busandbt.code.DeliveryType
import kotlinx.parcelize.Parcelize

/**
 * @param deliveryTypeList 배달유형 리스트 [DeliveryType]
 */
@Parcelize
data class NewShopListItem(
    val id: Int,
    val name: String,
    val deliveryTypeList: List<Int>,
    val imageUrl: String,
    val minDeliveryCost: Int,
    val hasCoupon: Boolean,
    val orderDoneMinutes: Int
) : Parcelable