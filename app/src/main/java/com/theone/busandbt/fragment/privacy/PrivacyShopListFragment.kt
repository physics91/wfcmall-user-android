package com.theone.busandbt.fragment.privacy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentPrivacyShopListBinding
import com.theone.busandbt.extension.commonWebClient
import com.theone.busandbt.fragment.DataBindingFragment

class PrivacyShopListFragment : DataBindingFragment<FragmentPrivacyShopListBinding>() {
    override val layoutId: Int = R.layout.fragment_privacy_shop_list

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            privacyShopListCloseButton.setOnClickListener {
                findNavController().popBackStack()
            }
            privacyShopListWebView.settings.javaScriptEnabled = true
            privacyShopListWebView.webViewClient = commonWebClient()
            privacyShopListWebView.loadUrl("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/privacy/shop_list.html")
        }
    }
}