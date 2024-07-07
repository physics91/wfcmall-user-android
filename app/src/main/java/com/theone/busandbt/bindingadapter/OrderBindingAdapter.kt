package com.theone.busandbt.bindingadapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.busandbt.code.CancelType
import com.busandbt.code.OrderStatus
import com.theone.busandbt.dto.order.OrderMenuInDetail
import com.theone.busandbt.extension.desc
import com.theone.busandbt.utils.LOCAL_DATE_TIME_FORMATTER
import java.time.format.DateTimeFormatter

/**
 * 주문 관련 기능의 BindingAdapter
 */
object OrderBindingAdapter {

    private val ORDER_LIST_DATE_FORMATTER = DateTimeFormatter.ofPattern("yy. MM. dd")

    /**
     * 주문 상태 문구 결정
     */
    @BindingAdapter("orderStatusText", "cancelType")
    @JvmStatic
    fun orderStatusTextForm(view: TextView, orderStatus: Int, cancelType: Int?) {
        view.text = when (orderStatus) {
            OrderStatus.NEW_OR_RECEIVING.id -> ""
            OrderStatus.START_COOKING.id -> "매장에서 맛있게 조리중이에요."
            OrderStatus.COMPLETE_PACKAGING.id -> "포장준비가 완료되었어요. 직접 픽업해주세요!"
            OrderStatus.COMPLETE_DELIVERY.id -> "배달이 완료되었어요!"
            OrderStatus.COMPLETE_PICK_UP.id -> "포장 주문이 완료되었어요!"
            OrderStatus.START_DELIVERY.id -> "배달이 시작되었어요."
            OrderStatus.CANCEL.id -> {
                val a = if (cancelType != null) CancelType.find(cancelType).desc() else ""
                buildString {
                    append("주문이 취소되었어요.")
                    if (a.isNotEmpty()) append(" (${a})")
                }
            }
            else -> ""
        }
    }

    /**
     * 주문 상태 문구 결정
     */
    @BindingAdapter("orderMenuList")
    @JvmStatic
    fun orderMenuListTextForm(view: TextView, menuList: List<OrderMenuInDetail>?) {
        if (menuList == null) return
        view.text = buildString {
            if (menuList.isEmpty()) return@buildString
            append(menuList.first().menuName)
            val s = menuList.size
            if (s > 1) append(" 외 ${s - 1}건")
        }
    }

    @BindingAdapter("orderListDateTime")
    @JvmStatic
    fun orderListDateTimeForm(view: TextView, dateTime: String) {
        view.text = ORDER_LIST_DATE_FORMATTER.format(LOCAL_DATE_TIME_FORMATTER.parse(dateTime))
    }
}