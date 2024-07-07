package com.theone.busandbt.extension

import com.google.api.client.http.HttpStatusCodes
import com.theone.busandbt.dto.ErrorResponse
import com.theone.busandbt.eventbus.ShowMessageDialogEvent
import com.theone.busandbt.eventbus.ToggleProgressEvent
import com.theone.busandbt.utils.JACKSON_OBJECT_MAPPER
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * API를 실행하고 성공일 때 로직을 결정하는 간단한 함수
 * TODO 실패했을 경우의 처리가 필요하다. 나중에 어느정도 코드가 정리되면 생각할 것
 */
fun <T> Call<T>.callOnSuccess(
    showProgress: Boolean = true,
    showFailMessage: Boolean = false,
    onFail: ((code: Int, rawData: String?) -> Unit)? = null,
    onSuccess: (responseData: T) -> Unit = {}
) {
    if (showProgress) EventBus.getDefault().post(ToggleProgressEvent(true))
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (showProgress) EventBus.getDefault().post(ToggleProgressEvent(false))
            if (response.code() != HttpStatusCodes.STATUS_CODE_OK) {
                if (showFailMessage) {
                    val errorResponse =
                        JACKSON_OBJECT_MAPPER.readValue(
                            response.errorBody()?.string(),
                            ErrorResponse::class.java
                        )
                    if (errorResponse != null) EventBus.getDefault().post(
                        ShowMessageDialogEvent(
                            title = errorResponse.message,
                            showWarningImageView = true
                        )
                    )
                }
                onFail?.invoke(response.code(), response.errorBody()?.string())
            }
            val data = response.body() ?: return
            onSuccess(data)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            if (showProgress) EventBus.getDefault().post(ToggleProgressEvent(false))
            t.printStackTrace()
        }
    })
}