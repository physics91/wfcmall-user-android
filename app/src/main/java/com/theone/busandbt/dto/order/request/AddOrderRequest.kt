package com.theone.busandbt.dto.order.request

import android.os.Parcelable
import com.busandbt.code.*
import kotlinx.parcelize.Parcelize

/**
 * 회원 주문 요청 데이터
 */
@Parcelize
data class AddOrderRequest(
    val memberId: Int,
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val serviceType: Int,
    val deliveryType: Int,
    val customerName: String?,
    val customerTel: String,
    val jibun: String,
    val road: String,
    val addressDetail: String,
    val customerLat: Double,
    val customerLng: Double,
    val paymentType: Int,
    val shopMemo: String,
    val riderMemo: String,
    val orderShopList: List<AddOrderShopRequest>,
    val useSecurityTel: Boolean,
    val doSendOrderInfo: Boolean,
    var orderId: String? = null,
    var cardInfoId: Int? = null,
    var cardSimplePassword: String? = null,
    var dongBaekGeonPaymentInfo: DongBaekGeonPaymentInfo? = null
) : Parcelable {

    /**
     * 주문 상품 데이터 (요청용)
     * @param menuId 상품 고유번호
     * @param count 이 상품의 개수
     * @param optionIdList 상품 옵션 id 리스트
     */
    @Parcelize
    data class OrderMenu(
        val menuId: Int,
        val count: Int,
        val optionIdList: List<Int> = emptyList()
    ) : Parcelable
}