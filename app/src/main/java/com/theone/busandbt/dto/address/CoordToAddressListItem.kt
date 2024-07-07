package com.theone.busandbt.dto.address

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoordToAddressListItem(
    @JsonAlias("address") val jibun: Address?,
    @JsonAlias("road_address") val road: RoadAddress?
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Address(
        @JsonAlias("address_name") val name: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RoadAddress(
        @JsonAlias("address_name") val name: String,
        @JsonAlias("building_name") val buildingName: String
    )
}