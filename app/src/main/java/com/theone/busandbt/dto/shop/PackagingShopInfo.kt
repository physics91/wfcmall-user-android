package com.theone.busandbt.dto.shop

/**
 * 포장주문 상점 정보 (포장주문 지도위에 표시되는 리스트 아이템)
 */
data class PackagingShopInfo(
    val id: Int,
    val name: String,
    val deliveryTypeList: List<Int>,
    val packagingDiscountCost: Int,
    val star: Double,
    val hasCoupon: Boolean,
    val imageUrl: String
)