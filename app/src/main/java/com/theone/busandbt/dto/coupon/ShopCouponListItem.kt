package com.theone.busandbt.dto.coupon

/**
 * 상점이 발급하는 쿠폰 데이터
 * @param name 쿠폰명
 * @param deliveryTypeList 쿠폰을 사용할 수 있는 배달유형 리스트
 * @param discountCost 쿠폰 할인 금액
 * @param minOrderCost 쿠폰 사용시에 최소주문금액 제한
 * @param useStartDateTime 사용기간 시작날짜
 * @param useEndDateTime 사용기간 종료날짜
 * @param availableDays 쿠폰 사용가능일
 */
data class ShopCouponListItem(
    val id: Int,
    val name: String,
    val deliveryTypeList: List<Int>,
    val discountCost: Int,
    val minOrderCost: Int,
    val remainCount: Int,
    val couponUseType: Int,
    val createDateTime: String,
    val useStartDateTime: String?,
    val useEndDateTime: String?,
    val availableDays: Int,
    val downloaded: Boolean,
    val issuedDateTime: String?
)