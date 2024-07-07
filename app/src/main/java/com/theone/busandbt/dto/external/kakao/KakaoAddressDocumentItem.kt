package com.theone.busandbt.dto.external.kakao

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kotlinx.parcelize.Parcelize

@JsonIgnoreProperties(ignoreUnknown = true)
@Parcelize
data class KakaoAddressDocumentItem(
    @JsonAlias("address_name") val name: String,             // 장소명, 업체명
    @JsonAlias("address") val jibun: JibunAddress?,           // 전체 지번 주소
    @JsonAlias("road_address") val road: RoadAddress?,      // 전체 도로명 주소
    @JsonAlias("x") val lng: String,                      // 경도 도로명 주소
    @JsonAlias("y") val lat: String                       // 위도 도로명 주소
) : Parcelable {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Parcelize
    data class JibunAddress(
        @JsonAlias("address_name") val name: String
    ) : Parcelable

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Parcelize
    data class RoadAddress(
        @JsonAlias("address_name") val name: String
    ) : Parcelable
}