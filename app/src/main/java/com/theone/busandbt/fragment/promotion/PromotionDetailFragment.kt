package com.theone.busandbt.fragment.promotion

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.PromotionAPI
import com.theone.busandbt.databinding.FragmentPromotionDetailBinding
import com.theone.busandbt.extension.commonWebClient
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DataBindingFragment
import org.koin.android.ext.android.inject

/**
 * 이벤트 배너 상세보기 화면
 */
class PromotionDetailFragment : DataBindingFragment<FragmentPromotionDetailBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_promotion_detail
    override var actionBarTitle: String = "배너상세"
    private val args: PromotionDetailFragmentArgs by navArgs()
    private val promotionAPI: PromotionAPI by inject()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            promotionWebView.apply {
                webViewClient = commonWebClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
            val landingUrl = args.landingUrl
            when {
                !landingUrl.isNullOrEmpty() -> {
                    promotionWebView.loadUrl(landingUrl)
                }
                args.promotionId > 0 -> {
                    safeApiRequest(
                        promotionAPI.getPromotionDetail(args.promotionId),
                        onFail = { _, _ ->
                            onNotExistLandingUrl()
                        }
                    ) {
                        if (it.landingUrl.isEmpty()) {
                            onNotExistLandingUrl()
                            return@safeApiRequest
                        }
                        promotionWebView.loadUrl(it.landingUrl)
                    }
                }
                else -> {
                    onNotExistLandingUrl()
                    return
                }
            }
        }
    }

    /**
     * 해당 프로모션에 랜딩 페이지가 존재하지 않을때 처리를 정의한다.
     * 1. 랜딩 페이지가 없을때는 배너상세 화면이 보여선 안된다.
     */
    private fun onNotExistLandingUrl() {
        findNavController().popBackStack()
    }
}