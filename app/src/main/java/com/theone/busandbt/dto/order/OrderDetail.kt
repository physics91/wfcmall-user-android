package com.theone.busandbt.dto.order

import com.theone.busandbt.extension.isWriteableReviewDateTime
import com.theone.busandbt.extension.toLocalDateTime
import java.time.LocalDateTime

/**
 * 주문 상세 조회
 */
data class OrderDetail(
    val shopId: Int,
    val shopName: String,
    val shopTel: String,
    val status: Int,
    val serviceType: Int,
    val deliveryType: Int,
    val customerTel: String, // 소비자 연락처
    val securityTel: String,
    val paymentType: Int,
    val menuCost: Int,
    val deliveryCost: Int,
    val extraCost: Int,
    val packagingDiscountCost: Int,
    val useCouponList: List<OrderCoupon>,
    val paymentCost: Int, // 결제 금액
    val shopMemo: String,
    val doWrittenReview: Boolean,
    val cancelType: Int?,
    val cancelReason: String?,
    val orderDoneDateTime: String?,
    val createDateTime: String,
    val menuList: List<OrderMenuInDetail>,
    val jibun: String? = null,
    val road: String? = null,
    val addressDetail: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val shopJibun: String? = null,
    val shopRoad: String? = null,
    val shopAddressDetail: String? = null,
    val shopLat: Double? = null,
    val shopLng: Double? = null,
    val riderMemo: String? = null,
) {

    /**
     * 지금 날짜 데이터가 리뷰 작성이 가능한 날짜인지 체크한다.
     */
    fun isWriteableReview(): Boolean {
        val testDateTime = orderDoneDateTime ?: return false
        return LocalDateTime.now().isWriteableReviewDateTime(testDateTime.toLocalDateTime()) && !doWrittenReview
    }
}