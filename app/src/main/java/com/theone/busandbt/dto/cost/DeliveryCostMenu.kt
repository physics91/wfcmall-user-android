package com.theone.busandbt.dto.cost

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeliveryCostMenu(
    val menuId: Int,
    val count: Int,
    val optionIdList: List<Int> = emptyList(),
    val menuCostId: Int
) : Parcelable
