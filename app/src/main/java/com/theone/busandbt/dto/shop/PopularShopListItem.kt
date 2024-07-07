package com.theone.busandbt.dto.shop

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PopularShopListItem(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val orderDoneMinutes: Int
) : java.io.Serializable, Parcelable