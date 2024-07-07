package com.theone.busandbt.dto.external.kakao

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.theone.busandbt.dto.address.AddressSearchResultItem

// 장소명, 주소 클래스
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoPlaceSearchResult(
    val documents: List<KakaoPlaceDocumentItem>          // 검색 결과
) {
    fun toAddressSearchResultList() = documents.map {
        AddressSearchResultItem(
            name = it.name,
            jibun = it.jibun,
            road = it.road,
            lng = it.lng,
            lat = it.lat
        )
    }
}