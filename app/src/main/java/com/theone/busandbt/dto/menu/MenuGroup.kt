package com.theone.busandbt.dto.menu

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuGroup(
    val name: String,
    val desc: String,
    val visibleIndex: Int,
    val menuList: List<Menu>
) : Parcelable