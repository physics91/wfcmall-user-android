package com.theone.busandbt.dto.address

data class ServerDeliveryAddress(
    val id: Int,
    val memberId: Int,
    val name: String,
    val jibun: String,
    val road: String,
    val addressDetail: String,
    val lat: Double,
    val lng: Double,
    val enabled: Boolean
) {
    fun toLocalDeliveryAddress() =
        DeliveryAddress(0, id, memberId, name, jibun, road, addressDetail, lat, lng, enabled)
}