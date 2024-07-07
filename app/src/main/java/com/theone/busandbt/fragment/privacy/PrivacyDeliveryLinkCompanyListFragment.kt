package com.theone.busandbt.fragment.privacy

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentPrivacyDeliveryLinkCompanyListBinding
import com.theone.busandbt.extension.commonWebClient
import com.theone.busandbt.fragment.DataBindingFragment

class PrivacyDeliveryLinkCompanyListFragment :
    DataBindingFragment<FragmentPrivacyDeliveryLinkCompanyListBinding>() {
    override val layoutId: Int = R.layout.fragment_privacy_delivery_link_company_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            privacyDeliveryLinkCompanyListCloseButton.setOnClickListener {
                findNavController().popBackStack()
            }
            privacyDeliveryLinkCompanyListWebView.webViewClient = commonWebClient()
            privacyDeliveryLinkCompanyListWebView.loadUrl("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/privacy/delivery_link_company_list.html")
        }
    }
}