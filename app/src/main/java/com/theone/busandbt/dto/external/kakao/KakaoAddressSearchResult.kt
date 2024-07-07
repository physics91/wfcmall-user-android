package com.theone.busandbt.dto.external.kakao

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.theone.busandbt.dto.address.AddressSearchResultItem

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoAddressSearchResult(
    val documents: List<KakaoAddressDocumentItem>
) {
    fun toAddressSearchResultList() = documents.mapNotNull {
        AddressSearchResultItem(
            name = it.name,
            jibun = it.jibun?.name ?: return@mapNotNull null,
            road = it.road?.name ?: return@mapNotNull null,
            lng = it.lng,
            lat = it.lat
        )
    }
}