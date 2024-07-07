package com.theone.busandbt.fragment.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannedString
import android.view.View
import android.webkit.JavascriptInterface
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.databinding.FragmentPaymentBinding
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.order.OrderListMainFragmentArgs
import com.theone.busandbt.fragment.order.OrderStatusFragmentArgs
import com.theone.busandbt.spanned.TypefaceSpanCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * 결제 화면
 * 주문번호는 이미 결정된 상태여야한다.
 */
class PaymentFragment : DataBindingFragment<FragmentPaymentBinding>() {
    override val layoutId: Int = R.layout.fragment_payment
    private val args by navArgs<PaymentFragmentArgs>()
    private val orderAPI: OrderAPI by inject()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val ctx = requireContext()
            paymentOkTextView.text = buildSpannedString {
                append("결제 진행 후,\n")
                append(
                    "결제 완료 ",
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(ctx, R.font.sult_bold) ?: return@with
                    ),
                    SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("버튼을 눌러주세요.")
            }

            paymentDoneButton.setOnClickListener {

            }

            paymentWebView.settings.javaScriptEnabled = true
            paymentWebView.webViewClient = commonWebClient()
            paymentWebView.addJavascriptInterface(Bridge(), "Payment")
            when {
                args.paymentUrl != null -> paymentWebView.loadUrl(args.paymentUrl!!)
                args.paymentHtml != null -> paymentWebView.loadData(
                    args.paymentHtml!!,
                    "text/html; charset=utf-8",
                    "UTF-8"
                )

                else -> {}
            }
        }
    }

    private fun addOrder() {
        val innerLoginInfo = loginInfo ?: return
        safeApiRequest(
            orderAPI.addOrder(innerLoginInfo.getFormedToken(), args.addOrderRequest)
        ) {
            basketListViewModel.removeSelectedMenu(
                basketListViewModel.getSelectedBasketShop(
                    args.serviceTypeId,
                    args.deliveryTypeId
                ) ?: return@safeApiRequest
            )
            val controller = findNavController()
            val dest = controller.currentDestination ?: return@safeApiRequest
            when (args.serviceTypeId) {
                ServiceType.SHOPPING_MALL.id -> {
                    controller.popBackStack(R.id.basket_graph, true)
                    controller.navigate(
                        R.id.order_list_graph,
                        OrderListMainFragmentArgs(
                            args.serviceTypeId,
                            args.deliveryTypeId
                        ).toBundle(),
                        NavOptions.Builder().setPopUpTo(dest.id, false).build()
                    )
                }

                else -> {
                    controller.popBackStack(R.id.basket_graph, true)
                    controller.navigate(
                        R.id.order_list_graph,
                        OrderListMainFragmentArgs(
                            args.serviceTypeId,
                            args.deliveryTypeId
                        ).toBundle(),
                        NavOptions.Builder().setPopUpTo(dest.id, false).build()
                    )
                    controller.navigate(
                        R.id.order_status_graph,
                        OrderStatusFragmentArgs(args.orderId).toBundle(),
                        NavOptions.Builder().setPopUpTo(dest.id, false).build()
                    )
                }
            }
        }
    }

    inner class Bridge {

        @JavascriptInterface
        fun completePayment(resultCode: String, resultMessage: String) {
            debugLog("결제", "결과코드 : $resultCode, 결과메시지 : $resultMessage")
            if (resultCode == "0000") {
                addOrder()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    with(findNavController()) {
                        popBackStack()
                        navigate(
                            R.id.payment_fail_graph,
                            PaymentFailFragmentArgs(
                                resultCode,
                                resultMessage,
                                args.serviceTypeId,
                                args.deliveryTypeId
                            ).toBundle()
                        )
                    }
                }
            }
        }
    }
}