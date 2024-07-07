package com.theone.busandbt.dto.shop

/**
 * 상점 위치 데이터
 * @param id 상점의 고유번호, shopId
 * @param lat 위도
 * @param lng 경도
 */
data class ShopLocation(
    val id: Int,
    val lat: Double,
    val lng: Double
)