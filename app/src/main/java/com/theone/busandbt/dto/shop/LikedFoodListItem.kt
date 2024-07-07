package com.theone.busandbt.dto.shop

import android.os.Parcelable
import com.busandbt.code.DeliveryType
import kotlinx.parcelize.Parcelize

/**
 * 음식점 찜 목록 데이터
 * @param deliveryTypeList 배달유형 리스트 [DeliveryType]
 */
@Parcelize
data class LikedFoodListItem(
    val id: Int,
    val name: String,
    val newDisplayDateTime: String?,
    val minOrderCost: Int,
    val deliveryDoneMinutes: Int,
    val packagingDoneMinutes: Int,
    val bundleDoneMinutes: Int,
    val minDeliveryCost: Int,
    val maxDeliveryCost: Int,
    val imageUrl: String,
    val hasCoupon: Boolean,
    val star: Double,
    val hasPackagingDiscount: Boolean,
    val deliveryTypeList: List<Int>,
    val isInOperation: Boolean
) : Parcelable