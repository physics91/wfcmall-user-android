package com.theone.busandbt.dto.address.response

import com.theone.busandbt.dto.address.CoordToAddressListItem
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoCoordToAddressResponse(
    val documents: List<CoordToAddressListItem>
)