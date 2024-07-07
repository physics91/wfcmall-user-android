package com.theone.busandbt.dto.address

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoordToRegionListItem(
    @JsonAlias("region_type") val regionType: String,
    @JsonAlias("address_name") val addressName: String,
    @JsonAlias("region_3depth_name") val region: String,
    @JsonAlias("x") val lng: Double,
    @JsonAlias("y") val lat: Double
)