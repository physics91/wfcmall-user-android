package com.theone.busandbt.api.payment

import com.theone.busandbt.dto.payment.request.ApprovalDongBaekGeonRequest
import com.theone.busandbt.dto.payment.request.SendOrderDongBaekGeonRequest
import com.theone.busandbt.dto.payment.response.ApprovalDongBaekGeonResponse
import com.theone.busandbt.dto.payment.response.SendOrderDongBaekGeonResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentAPI {

    /**
     * 동백전 API 주문정보전달
     */
    @POST("/dongbaekgeon/order")
    fun sendOrder(
        @Body request: SendOrderDongBaekGeonRequest
    ): Call<SendOrderDongBaekGeonResponse>

    @POST("/dongbaekgeon/approval")
    fun approval(
        @Body request: ApprovalDongBaekGeonRequest
    ): Call<ApprovalDongBaekGeonResponse>
}