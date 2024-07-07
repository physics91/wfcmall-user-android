package com.theone.busandbt.api.loginchannel

import com.theone.busandbt.dto.version.VersionInfoDetail
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface VersionAPI {

    @GET("/version/latest/{platformType}")
    fun getLatestVersionInfo(
        @Path("platformType") platformType: Int = 1,
    ): Call<VersionInfoDetail>
}