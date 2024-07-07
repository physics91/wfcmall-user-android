package com.theone.busandbt.dto.cost

data class TimeExtra(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val cost: Int
)