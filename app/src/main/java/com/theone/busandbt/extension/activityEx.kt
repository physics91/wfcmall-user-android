package com.theone.busandbt.extension

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import com.theone.busandbt.dialog.MessageDialog
import retrofit2.Call
import java.lang.ref.WeakReference

fun AppCompatActivity.showMessageDialog(
    title: String,
    subTitle: String = "",
    showCancelButton: Boolean = false,
    showWarningImageView: Boolean = false,
    cancelButtonText: String? = null,
    cancelable: Boolean = true,
    op: MessageDialog.() -> Unit = {}
): MessageDialog {
    return MessageDialog(
        title,
        subTitle,
        showCancelButton,
        showWarningImageView,
        cancelButtonText,
        cancelable
    ).apply {
        show(this@showMessageDialog.supportFragmentManager, null)
        op(this)
    }
}

fun <Response> Activity.safeApiRequest(
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
        val activity = weak.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) return@callOnSuccess
        op(it)
    }
}