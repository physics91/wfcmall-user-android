package com.theone.busandbt.api.loginchannel

import com.theone.busandbt.dto.cert.request.AddCertNumberRequest
import com.theone.busandbt.dto.cert.request.CheckCertNumberRequest
import com.theone.busandbt.dto.cert.response.CheckCertNumberResponse
import com.theone.busandbt.utils.HTTP_HEADER_APPLICATION_JSON
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * 인증 API
 */
interface CertAPI {

    /**
     * 휴대폰 인증번호 인증 요청
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/cert/phone/check")
    fun checkCertNumber(@Body request: CheckCertNumberRequest): Call<CheckCertNumberResponse>

    /**
     * 휴대폰 인증번호 발급/재발급
     */
    @Headers(HTTP_HEADER_APPLICATION_JSON)
    @POST("/cert/phone")
    fun addCertNumber(@Body request: AddCertNumberRequest): Call<Unit>
}