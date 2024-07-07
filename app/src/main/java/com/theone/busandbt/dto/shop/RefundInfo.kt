package com.theone.busandbt.dto.shop

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RefundInfo(
    val title: String,
    val desc: String
) : Parcelable