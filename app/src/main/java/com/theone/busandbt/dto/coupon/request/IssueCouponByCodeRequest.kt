package com.theone.busandbt.dto.coupon.request

data class IssueCouponByCodeRequest(
    val memberId: Int,
    val code: String
)