package com.theone.busandbt.dto.menu

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuInfo(
    val title: String,
    val desc: String
) : Parcelable