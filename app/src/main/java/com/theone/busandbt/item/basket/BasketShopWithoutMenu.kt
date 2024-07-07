package com.theone.busandbt.item.basket

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "basket_shop")
data class BasketShopWithoutMenu(
    @PrimaryKey(autoGenerate = true) var id: Long,
    val shopId: Int,
    val shopName: String,
    val serviceTypeId: Int,
    val deliveryTypeId: Int,
    val deliveryCost: Int,
    val minOrderCost: Int,
    var isSelected: Boolean
)