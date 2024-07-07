package com.theone.busandbt.instance

import java.time.format.DateTimeFormatter
import java.util.Locale

val HOUR_MINUTES_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val KOREAN_LOCAL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
val KOREAN_LOCAL_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA)