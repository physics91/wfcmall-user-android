package com.theone.busandbt.fragment.privacy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import androidx.lifecycle.lifecycleScope
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentPrivacyDetailBinding
import com.theone.busandbt.extension.commonWebClient
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.DataBindingFragment

class PrivacyDetailFragment : DataBindingFragment<FragmentPrivacyDetailBinding>() {
    override val layoutId: Int = R.layout.fragment_privacy_detail
    override val actionBarTitle: String = "개인정보 수집 및 이용 동의"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            privacyDetailWebView.settings.javaScriptEnabled = true
            privacyDetailWebView.webViewClient = commonWebClient()
            privacyDetailWebView.addJavascriptInterface(Bridge(), "Privacy")
            privacyDetailWebView.loadUrl("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/privacy/privacy_service_agree_2.html")
        }
    }

    inner class Bridge {

        @JavascriptInterface
        fun goCompanyList(type: Int) {
            lifecycleScope.launchWhenStarted {
                view?.navigate(
                    when (type) {
                        1 -> R.id.privacy_shop_list_graph
                        2 -> R.id.privacy_delivery_company_list_graph
                        3 -> R.id.privacy_delivery_link_company_list_graph
                        else -> return@launchWhenStarted
                    }
                )
            }
        }
    }
}