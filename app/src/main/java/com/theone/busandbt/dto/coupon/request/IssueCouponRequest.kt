package com.theone.busandbt.dto.coupon.request

data class IssueCouponRequest(
    val memberId: Int? = null,
    val memberLoginId: String? = null,
    val couponIdList: List<Int>
)