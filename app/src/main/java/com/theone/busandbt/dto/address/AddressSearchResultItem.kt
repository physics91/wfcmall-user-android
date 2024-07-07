package com.theone.busandbt.dto.address

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressSearchResultItem(
    val name: String,
    val jibun: String,
    val road: String,
    val lng: String,
    val lat: String
) : Parcelable