package com.theone.busandbt.item.basket

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "basket_menu")
data class BasketMenuWithoutOption(
    @PrimaryKey(autoGenerate = true) var id: Long,
    val basketShopId: Long,
    val menuId: Int,
    val status: Int,
    val name: String,
    val isAdultMenu: Boolean,
    val imageUrl: String,
    val cost: Int,
    val saleCost: Int,
    var isSelected: Boolean,
    var count: Int,
    val menuCostId: Int,
    val menuCostName: String
)