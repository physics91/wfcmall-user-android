package com.theone.busandbt.dto.order.request

import android.os.Parcelable
import com.theone.busandbt.dto.cost.DeliveryCostMenu
import kotlinx.parcelize.Parcelize

/**
 * TODO - 이벤트 쿠폰 같은 경우 어디쪽으로 할인시켜야 하는가
 */
@Parcelize
data class AddOrderShopRequest(
    val shopId: Int,
    val useCouponIdList: List<Int>,
    val menuList: List<DeliveryCostMenu>
) : Parcelable