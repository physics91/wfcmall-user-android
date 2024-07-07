package com.theone.busandbt.dto.coupon.request

data class DownloadCouponListRequest(
    val memberId: Int,
    val couponIdList: List<Int>
)