package com.theone.busandbt.dto.menu

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuOption(
    val id: Int,
    val name: String,
    val status: Int,
    val cost: Int,
    var isSelected: Boolean,
    val visibleIndex: Int,
) : Parcelable