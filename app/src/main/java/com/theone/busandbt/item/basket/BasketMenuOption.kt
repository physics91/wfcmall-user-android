package com.theone.busandbt.item.basket

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "basket_menu_option")
data class BasketMenuOption(
    @PrimaryKey(autoGenerate = true) var id: Long,
    var basketMenuId: Long,
    val menuId: Int,
    val optionId: Int,
    val optionGroupName: String,
    val name: String,
    var status: Int,
    var cost: Int,
) {
    
    companion object {
        fun createForAdd(
            menuId: Int,
            optionId: Int,
            optionGroupName: String,
            name: String,
            status: Int,
            cost: Int,
        ) = BasketMenuOption(0, 0, menuId, optionId, optionGroupName, name, status, cost)
    }
}