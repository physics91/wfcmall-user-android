package com.theone.busandbt.extension

import com.busandbt.code.ShopSortType
import com.theone.busandbt.instance.HOUR_MINUTES_TIME_FORMATTER
import com.theone.busandbt.instance.KOREAN_LOCAL_DATE_FORMATTER
import com.theone.busandbt.instance.KOREAN_LOCAL_TIME_FORMATTER
import com.theone.busandbt.utils.EMAIL_REGEX
import com.theone.busandbt.utils.LOCAL_DATE_TIME_FORMATTER
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.regex.Pattern

/**
 * 문자열을 [LocalDateTime]형태로 변경한다.
 * 단, yyyy-MM-dd HH:mm:ss 형식의 문자열로만 해야한다.
 */
fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, LOCAL_DATE_TIME_FORMATTER)

fun String.toHourMinuteString(): String = LocalTime.parse(this).format(HOUR_MINUTES_TIME_FORMATTER)

fun String.toKoreanDateString(): String = LocalDate.parse(this).format(KOREAN_LOCAL_DATE_FORMATTER)

fun String.toKoreanTimeString(): String = LocalTime.parse(this).format(KOREAN_LOCAL_TIME_FORMATTER)

fun String.isEmail(): Boolean = Pattern.matches(EMAIL_REGEX, this)

@Deprecated("사용안할 예정", replaceWith = ReplaceWith(""))
fun String.findShopSortType(): ShopSortType? = when (this) {
    "가까운 순" -> ShopSortType.NEAR
    "배달 빠른 순" -> ShopSortType.FAST_DELIVERY
    "배달비 낮은 순" -> ShopSortType.LOWER_DELIVERY_COST
    "주문 많은 순" -> ShopSortType.MANY_ORDER
    "별점 높은 순" -> ShopSortType.HIGHER_STAR
    "찜 많은 순" -> ShopSortType.MANY_LIKE
    else -> null
}

fun String.getJibunDetail() = with(split(" ")) {
    if (size < 3) return@with ""
    slice(3 until size).joinToString(" ")
}