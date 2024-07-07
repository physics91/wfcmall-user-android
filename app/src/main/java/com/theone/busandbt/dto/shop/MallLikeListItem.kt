package com.theone.busandbt.dto.shop

/**
 * 쇼핑몰 찜 목록 데이터
 */
data class MallLikeListItem(
    val id: Int,
    val name: String,
    val status: Int,
    val star: Double,
    val imageUrl: String,
    val deliveryCost: Int,
    val orderCostForFree: Int,
)