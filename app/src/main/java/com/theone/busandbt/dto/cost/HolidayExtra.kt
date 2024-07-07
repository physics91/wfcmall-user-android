package com.theone.busandbt.dto.cost

data class HolidayExtra(
    val legalHolidayName: String,
    val legalHolidayDate: String,
    val startDate: String?,
    val endDate: String?,
    val cost: Int
)