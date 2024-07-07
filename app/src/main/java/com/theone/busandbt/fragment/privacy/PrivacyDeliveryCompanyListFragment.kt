package com.theone.busandbt.fragment.privacy

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentPrivacyDeliveryCompanyListBinding
import com.theone.busandbt.extension.commonWebClient
import com.theone.busandbt.fragment.DataBindingFragment

class PrivacyDeliveryCompanyListFragment :
    DataBindingFragment<FragmentPrivacyDeliveryCompanyListBinding>() {
    override val layoutId: Int = R.layout.fragment_privacy_delivery_company_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            privacyDeliveryCompanyListCloseButton.setOnClickListener {
                findNavController().popBackStack()
            }
            privacyDeliveryCompanyListWebView.webViewClient = commonWebClient()
            privacyDeliveryCompanyListWebView.loadUrl("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/privacy/delivery_company_list.html")
        }
    }
}