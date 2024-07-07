package com.theone.busandbt.dto.order

import android.os.Parcelable
import com.theone.busandbt.extension.isWriteableReviewDateTime
import com.theone.busandbt.extension.toLocalDateTime
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class MallOrderShopInDetail(
    val sequence: Int,
    val shopId: Int,
    val shopName: String,
    val shopTel: String,
    val parcelType: Int,
    val parcelStatus: Int,
    val menuList: List<OrderMenuInDetail>,
    val deliveryCost: Int,
    val parcelCompleteDateTime: String?,
    val courierType: Int?,
    val waybillNo: String?,
    var doWrittenReview: Boolean
) : Parcelable {

    /**
     * 지금 날짜 데이터가 리뷰 작성이 가능한 날짜인지 체크한다.
     */
    fun isWriteableReview(): Boolean {
        val testDateTime = parcelCompleteDateTime ?: return false
        return LocalDateTime.now().isWriteableReviewDateTime(testDateTime.toLocalDateTime()) && !doWrittenReview
    }
}