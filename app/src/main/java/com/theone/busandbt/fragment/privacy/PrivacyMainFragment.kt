package com.theone.busandbt.fragment.privacy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import androidx.lifecycle.lifecycleScope
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentPrivacyMainBinding
import com.theone.busandbt.extension.commonWebClient
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.DataBindingFragment

class PrivacyMainFragment : DataBindingFragment<FragmentPrivacyMainBinding>() {
    override val layoutId: Int = R.layout.fragment_privacy_main
    override val actionBarTitle: String = "개인정보처리방침"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            privacyMainWebView.settings.javaScriptEnabled = true
            privacyMainWebView.webViewClient = commonWebClient()
            privacyMainWebView.addJavascriptInterface(Bridge(), "Privacy")
            privacyMainWebView.loadUrl("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/privacy/privacy_service_agree_1.html")
        }
    }

    inner class Bridge {

        @JavascriptInterface
        fun goPrivacyDetail() {
            lifecycleScope.launchWhenStarted {
                view?.navigate(R.id.privacy_detail_graph)
            }
        }
    }
}