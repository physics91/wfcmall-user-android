package com.theone.busandbt.item

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addressave_table")
data class AddressSaveItem(
    @PrimaryKey
    val id: Int,
    val address: String?,
)