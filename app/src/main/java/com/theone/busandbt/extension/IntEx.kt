package com.theone.busandbt.extension

import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.busandbt.code.CardType
import com.theone.busandbt.R
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val NUMBER_FORMATTER = DecimalFormat("#,###,###")

fun Int.dpToPx() = SizeUtils.dp2px(toFloat())
fun Int.toMoneyFormat(): String = NUMBER_FORMATTER.format(this)
fun Int.toCommonMoneyForm(): String = StringUtils.getString(R.string.commonCost, toMoneyFormat())

fun Int.signText(): String = when {
    this > 0 -> "+"
    this < 0 -> "-"
    else -> ""
}

fun Int?.toCardDesc(): String {
    return this?.toCardType()?.desc() ?: ""
}

fun Int.toCardType(): CardType? = CardType.VALUES[this]

fun Int.toDayOfWeekShort(): String =
    DayOfWeek.of(this).getDisplayName(TextStyle.SHORT, Locale.KOREA)

fun Int.positive(): Int = if (this < 0) 0 else this