package com.theone.busandbt.dto.shop

import android.os.Parcelable
import com.busandbt.code.DeliveryType
import kotlinx.parcelize.Parcelize

/**
 * 상점 리스트 데이터
 * @param id 상점 고유번호, shopId
 * @param minOrderCost 최소 주문 금액
 * @param deliveryTypeList 배달유형 리스트 [DeliveryType]
 * @param newDisplayDateTime 신규표시 날짜, 이 날짜까지만 신규 라벨을 표시한다.
 * @param star 별점, 5점만점에 0.5점 단위
 * @param imageUrl 상점 이미지 URL
 * @param minDeliveryCost 최소 배달료
 * @param maxDeliveryCost 최대 배달료
 */
@Parcelize
data class ShopListItem(
    val id: Int,
    val name: String,
    val minOrderCost: Int,
    val deliveryTypeList: List<Int>,
    val deliveryDoneMinutes: Int,
    val packagingDoneMinutes: Int,
    val bundleDoneMinutes: Int,
    val newDisplayDateTime: String?,
    val star: Double,
    val imageUrl: String,
    val minDeliveryCost: Int,
    val maxDeliveryCost: Int,
    val isInOperation: Boolean,
    val hasCoupon: Boolean,
    val hasPackagingDiscount: Boolean,
    val isGoodShop: Boolean,
    val searchedMenuNameList: List<String>? = null
) : Parcelable