package com.theone.busandbt.dto.address.request

data class AddDeliveryAddressRequest(
    val memberId: Int,
    val name: String,
    val jibun: String,
    val road: String,
    val addressDetail: String,
    val lat: Double,
    val lng: Double,
    val enabled: Boolean = true
)