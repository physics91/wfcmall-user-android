package com.theone.busandbt.dto.cost

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.parcelize.Parcelize

@JsonIgnoreProperties(ignoreUnknown = true)
@Parcelize
data class MenuCostListItem(
    val id: Int,
    val name: String,
    val cost: Int,
    val saleCost: Int,
    val discountRate: Int,
    var isSelected: Boolean = false
) : Parcelable {
    companion object {
        fun createEmpty() = MenuCostListItem(0, "", 0, 0, 0)
    }
}