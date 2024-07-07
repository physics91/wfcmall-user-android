package com.theone.busandbt.dto.shop.request

/**
 * 상점 즐겨찾기 RequestBody
 * @param memberId 즐겨찾기를 하는 회원
 * @param doLike 즐겨찾기 여부, true일 경우 즐겨찾기를 진행, false는 즐겨찾기 해제
 */
data class ShopLikeToggleRequest(
    val memberId: Int,
    val doLike: Boolean
)