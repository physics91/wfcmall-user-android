package com.theone.busandbt.dto.coupon

/**
 * 회원이 소유한 쿠폰 데이터
 */
data class MemberCouponByShop(
    val shopId: Int,
    val shopName: String,
    val couponList: List<CouponListItem>
) {
    data class CouponListItem(
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
        val sigungu: String?
    )

    fun toItemList(): List<MemberCouponListItem> = couponList.map {
        with(it) {
            MemberCouponListItem(
                shopId,
                shopName,
                id,
                type,
                name,
                categoryIdList,
                serviceType,
                deliveryTypeList,
                discountCost,
                minOrderCost,
                useType,
                useStartDateTime,
                useEndDateTime,
                availableDays,
                code,
                issuedDateTime,
                shopOverlapUseEnabled,
                eventOverlapUseEnabled,
                sigungu
            )
        }
    }
}