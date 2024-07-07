package com.theone.busandbt.dto.shop

data class HolidayListItem(
    val type: Int,
    val period: Int,
    val dayOfWeek: Int?,
    val applyDayOfMonth: Int,
)