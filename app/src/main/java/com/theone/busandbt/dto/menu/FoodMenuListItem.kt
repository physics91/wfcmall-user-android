package com.theone.busandbt.dto.menu

data class FoodMenuListItem(
    val id: Int,
    val name: String,
    val status: Int,
    val struct: String,
    val cost: Int,
    val imageUrl: String
)