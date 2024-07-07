package com.theone.busandbt.dto.cost

data class AreaExtra(
    val name: String,
    val cost: Int,
    val latLngList: List<AreaExtraLatLng>
)