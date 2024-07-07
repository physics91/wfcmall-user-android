package com.theone.busandbt.dto.address

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val jibun: String,
    val road: String,
    val lat: Double,
    val lng: Double
) : Parcelable