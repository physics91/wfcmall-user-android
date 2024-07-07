package com.theone.busandbt.dto.order

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MallOrderDetail(
    val status: Int,
    val deliveryType: Int,
    val customerTel: String, // 소비자 연락처
    val securityTel: String,
    val jibun: String,
    val road: String,
    val addressDetail: String,
    val riderMemo: String,
    val customerName: String,
    val paymentType: Int,
    val menuCost: Int,
    val deliveryCost: Int,
    val extraCost: Int,
    val paymentCost: Int, // 결제 금액
    val useCouponList: List<OrderCoupon>,
    val createDateTime: String,
    val orderShopList: List<MallOrderShopInDetail>
) : Parcelable