package com.theone.busandbt.function

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.os.bundleOf
import com.blankj.utilcode.util.StringUtils
import com.theone.busandbt.BuildConfig
import com.theone.busandbt.R
import com.theone.busandbt.extension.isEmail
import com.theone.busandbt.utils.ENABLE_TAB_ARRAY
import com.theone.busandbt.utils.MIN_USER_NAME_LENGTH
import com.theone.busandbt.utils.PHONE_NUMBER_LENGTH
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType

/**
 * 디버그 환경에서만 실행되는 코드를 정의한다.
 */
inline fun debugCode(op: () -> Unit) {
    if (BuildConfig.DEBUG) op()
}

fun getTabPosition(serviceTypeId: Int, deliveryTypeId: Int): Int {
    return when {
        serviceTypeId == ServiceType.FOOD_DELIVERY.id && deliveryTypeId == DeliveryType.INSTANT.id -> 0 // 음식점 바로배달
        serviceTypeId == ServiceType.FOOD_DELIVERY.id && deliveryTypeId == DeliveryType.PACKAGING.id -> 1 // 음식점 포장주문
        serviceTypeId == ServiceType.SHOPPING_MALL.id && deliveryTypeId == DeliveryType.PARCEL.id -> 2 // 쇼핑몰
        else -> 0 // 해당하는 사항이 없을 경우 기본 위치 0으로
    }
}

fun findTab(serviceType: ServiceType, deliveryType: DeliveryType) =
    findTab(serviceType.id, deliveryType.id)

fun findTab(serviceTypeId: Int, deliveryTypeId: Int) =
    ENABLE_TAB_ARRAY.find { it.serviceType.id == serviceTypeId && it.deliveryType.id == deliveryTypeId }

fun createTabArguments(tabPosition: Int): Bundle {
    val result = bundleOf()
    val (serviceType, deliveryType) = when (tabPosition) {
        0 -> ServiceType.FOOD_DELIVERY to DeliveryType.INSTANT
        1 -> ServiceType.FOOD_DELIVERY to DeliveryType.PACKAGING
        2 -> ServiceType.SHOPPING_MALL to DeliveryType.PARCEL
        else -> ServiceType.FOOD_DELIVERY to DeliveryType.INSTANT
    }
    result.putInt("serviceTypeId", serviceType.id)
    result.putInt("deliveryTypeId", deliveryType.id)
    return result
}

fun phoneTextWatcher(op: (text: String, passed: Boolean) -> Unit) = object : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun afterTextChanged(p0: Editable?) {
        val text = p0.toString()
        val passed = text.length == PHONE_NUMBER_LENGTH
        op(text, passed)
    }
}

fun nameTextWatcher(op: (text: String, passed: Boolean) -> Unit) = object : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun afterTextChanged(p0: Editable?) {
        val text = p0.toString()
        val passed = text.length >= MIN_USER_NAME_LENGTH
        op(text, passed)
    }
}

fun emailTextWatcher(op: (text: String, passed: Boolean) -> Unit) = object : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun afterTextChanged(p0: Editable?) {
        val text = p0.toString()
        val passed = text.isEmail()
        op(text, passed)
    }
}

fun getOrderDoneMinutesText(orderDoneMinutes: Int, dash: Boolean = false): String =
    StringUtils.getString(
        if (dash) R.string.orderDoneMinutesDashFormat else R.string.orderDoneMinutesFormat,
        (orderDoneMinutes - 5).takeIf { it >= 0 } ?: 0,
        orderDoneMinutes + 5
    )

fun formatPhoneNumber(s: String): String {
    val digits = s.filter { it.isDigit() }
    return when (digits.length) {
        11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
        12 -> "${digits.substring(0, 4)}-${digits.substring(4, 8)}-${digits.substring(8)}"
        else -> digits  // 다른 길이의 문자열은 그대로 반환
    }
}