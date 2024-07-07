package com.theone.busandbt.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.theone.busandbt.R
import com.theone.busandbt.databinding.DialogPrivacyForAddOrderBinding
import com.theone.busandbt.extension.commonWebClient

class PrivacyForAddOrderDialog(
    private val shopName: String
) : DataBindingDialog<DialogPrivacyForAddOrderBinding>() {
    override val layoutId: Int = R.layout.dialog_privacy_for_add_order

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            privacyCloseButton.setOnClickListener { dismiss() }
            privacyDoneButton.setOnClickListener { dismiss() }
            privacyWebView.settings.javaScriptEnabled = true
            privacyWebView.webViewClient = commonWebClient()
            privacyWebView.loadUrl("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/privacy/privacy_for_order.html?shopName=${shopName}")
        }
    }
}