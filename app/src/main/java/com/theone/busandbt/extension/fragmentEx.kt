package com.theone.busandbt.extension

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.theone.busandbt.dialog.MessageDialog
import com.theone.busandbt.fragment.DataBindingFragment
import kotlinx.coroutines.CoroutineScope
import retrofit2.Call
import java.lang.ref.WeakReference

fun Fragment.showMessageDialog(
    title: String,
    subTitle: String = "",
    showCancelButton: Boolean = false,
    showWarningImageView: Boolean = false,
    cancelButtonText: String? = null,
    cancelable: Boolean = true,
    op: MessageDialog.() -> Unit = {}
): MessageDialog = childFragmentManager.showMessageDialog(
    title,
    subTitle,
    showCancelButton,
    showWarningImageView,
    cancelButtonText,
    cancelable,
    op
)

inline fun Fragment.safe(
    crossinline op: CoroutineScope.() -> Unit
) {
    lifecycleScope.launchWhenStarted {
        op(this)
    }
}

val Fragment.packageManager get() = activity?.packageManager

// 웹뷰에서 URL 리다이렉션을 허용해주기 위함
fun Fragment.commonWebClient() = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false
        debugLog("url", url)
        if (url.startsWith("intent:")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val existPackage =
                    requireActivity().packageManager.getLaunchIntentForPackage(
                        intent.getPackage() ?: ""
                    )
                if (existPackage != null) {
                    startActivity(intent)
                } else {
                    val marketIntent = Intent(Intent.ACTION_VIEW)
                    marketIntent.data = Uri.parse(
                        "market://details?id=" +
                                intent.getPackage()
                    )
                    startActivity(marketIntent)
                }
                return true
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        } else {
            view?.loadUrl(request.url.toString())
        }
        return false

    }
}

fun <Response> DataBindingFragment<*>.safeApiRequest(
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
        if (fragment == null || !fragment.isAdded || fragment.view == null) return@callOnSuccess
        op(it)
    }
}