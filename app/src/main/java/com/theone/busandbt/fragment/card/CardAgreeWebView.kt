package com.theone.busandbt.fragment.card

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentCardAgreeWebViewBinding
import com.theone.busandbt.extension.commonWebClient
import com.theone.busandbt.fragment.DataBindingFragment

class CardAgreeWebView : DataBindingFragment<FragmentCardAgreeWebViewBinding>() {
    override val layoutId: Int = R.layout.fragment_card_agree_web_view
    private val args by navArgs<CardAgreeWebViewArgs>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            cardAgreeWebView.webViewClient = commonWebClient()
            cardAgreeWebView.loadUrl(args.url)
        }
    }
}