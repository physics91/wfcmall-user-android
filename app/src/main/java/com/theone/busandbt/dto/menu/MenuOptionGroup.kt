package com.theone.busandbt.dto.menu

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuOptionGroup(
    val name: String,
    val desc: String,
    val requiredChoice: Boolean,
    val maxChoiceCount: Int,
    val visibleIndex: Int,
    val optionList: List<MenuOption>
) : Parcelable