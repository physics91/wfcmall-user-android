package com.theone.busandbt.dto.shop

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryIdAndServiceType(
    val id: Int,
    val serviceType: Int
) : Parcelable