package com.theone.busandbt.dto.order.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DongBaekGeonPaymentInfo(
    val userId: String,
    val trNo: String,
    val secret: String,
) : Parcelable