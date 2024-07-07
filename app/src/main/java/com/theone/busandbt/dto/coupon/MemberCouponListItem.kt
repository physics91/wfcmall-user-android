package com.theone.busandbt.dto.coupon

data class MemberCouponListItem(
    val shopId: Int,
    val shopName: String,
    val id: Int,
    val type: Int,
    val name: String,
    val categoryIdList: List<Int>,
    val serviceType: Int,
    val deliveryTypeList: List<Int>,
    val discountCost: Int,
    val minOrderCost: Int,
    val useType: Int,
    val useStartDateTime: String?,
    val useEndDateTime: String?,
    val availableDays: Int,
    val code: String,
    val issuedDateTime: String,
    val shopOverlapUseEnabled: Boolean,
    val eventOverlapUseEnabled: Boolean,
    val sigungu: String?,
    var isSelected: Boolean = false
)