package com.theone.busandbt.dto.address.response

import com.theone.busandbt.dto.address.CoordToRegionListItem
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoCoordToRegionResponse(
    val documents: List<CoordToRegionListItem>
)