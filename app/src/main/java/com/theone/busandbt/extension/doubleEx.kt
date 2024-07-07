package com.theone.busandbt.extension

fun Double.formatDistance() = if (this >= 1.0) "${this}km" else "${(this * 1000).toInt()}m"