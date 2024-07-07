package com.theone.busandbt.extension

import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.theone.busandbt.dialog.selection.SelectionDialog
import retrofit2.Call
import java.lang.ref.WeakReference

inline fun <reified Output> SelectionDialog<*, Output>.setOnReceiveData(
    fragment: Fragment,
    noinline op: (Output?) -> Unit
) {
    onReceiveData(fragment, Output::class.java, op)
}

/**
 * 주의 : dialog 내에서 사용해야 하지만 fragment.dialog?.isShowing에 주의해야한다.
 * 부모 fragment 상태를 체크하고 싶을땐 이 것을 사용해선 안된다.
 */
fun <Response> AppCompatDialogFragment.safeApiRequest(
    api: Call<Response>,
    showFailMessage: Boolean = false,
    showProgress: Boolean = true,
    onFail: ((code: Int, rawData: String?) -> Unit)? = null,
    op: (Response) -> Unit = {}
) {
    val weak = WeakReference(this)
    api.callOnSuccess(
        onFail = onFail,
        showFailMessage = showFailMessage,
        showProgress = showProgress
    ) {
        val fragment = weak.get()
        if (fragment == null || !fragment.isAdded || fragment.dialog?.isShowing != true) return@callOnSuccess
        op(it)
    }
}