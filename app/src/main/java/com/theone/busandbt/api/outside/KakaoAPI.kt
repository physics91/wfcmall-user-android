package com.theone.busandbt.api.outside

import com.theone.busandbt.dto.address.response.KakaoCoordToAddressResponse
import com.theone.busandbt.dto.address.response.KakaoCoordToRegionResponse
import com.theone.busandbt.dto.external.kakao.KakaoAddressSearchResult
import com.theone.busandbt.dto.external.kakao.KakaoPlaceSearchResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoAPI {

    companion object {
        private const val API_KEY = "KakaoAK 7dd7031a727a9616fa2dd9d8308f2d70"  // REST API 키
    }

    @GET("v2/local/search/keyword.json")
    fun searchPlace(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("size") count: Int,
        @Header("Authorization") key: String = API_KEY
    ): Call<KakaoPlaceSearchResult>

    @GET("v2/local/search/address.json")
    fun searchAddress(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("size") count: Int,
        @Header("Authorization") key: String = API_KEY
    ): Call<KakaoAddressSearchResult>

    @GET("v2/local/geo/coord2address.json")
    fun getSearchLocalAddress(
        @Query("x") lng: String,
        @Query("y") lat: String,
        @Header("Authorization") key: String = API_KEY
    ): Call<KakaoCoordToAddressResponse>

    /**
     * 좌표로 행정동, 법정동 조회
     */
    @GET("v2/local/geo/coord2regioncode.json")
    fun getSearchRegion(
        @Query("x") lng: String,
        @Query("y") lat: String,
        @Header("Authorization") key: String = API_KEY
    ): Call<KakaoCoordToRegionResponse>
}