package com.theone.busandbt.dto.address.request

data class SetDeliveryAddressRequest(
    val name: String? = null,
    val jibun: String? = null,
    val road: String? = null,
    val addressDetail: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val enabled: Boolean? = null
)