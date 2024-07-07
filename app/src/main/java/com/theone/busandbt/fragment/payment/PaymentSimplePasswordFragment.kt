package com.theone.busandbt.fragment.payment

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.payment.PaymentPasswordInputProgressAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.databinding.FragmentPaymentSimplePasswordBinding
import com.theone.busandbt.dto.ErrorResponse
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.order.OrderListMainFragmentArgs
import com.theone.busandbt.fragment.order.OrderStatusFragmentArgs
import com.theone.busandbt.utils.CARD_SIMPLE_PASSWORD_LENGTH
import com.theone.busandbt.utils.JACKSON_OBJECT_MAPPER
import org.koin.android.ext.android.inject

/**
 * 사이다페이 결제 비밀번호 입력 화면
 */
class PaymentSimplePasswordFragment : DataBindingFragment<FragmentPaymentSimplePasswordBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_payment_simple_password
    override val actionBarTitle: String = "결제"
    private val args by navArgs<PaymentSimplePasswordFragmentArgs>()
    private val orderAPI: OrderAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val adapter = PaymentPasswordInputProgressAdapter()
            passwordInputProgressRecyclerView.adapter = adapter

            passwordForgetInfoTextView.setOnClickListener {
                showMessageDialog("카드결제 비밀번호 분실", "비밀번호 분실한 카드 삭제 후\n재등록 해주세요.")
            }

            securityKeyboard.setOnClickBackButton {
                adapter.removeLastNumber()
            }

            securityKeyboard.setOnClickNumberButton { _, number ->
                if (number == null) return@setOnClickNumberButton
                val changedIndex = adapter.addNumber(number)
                if (changedIndex == CARD_SIMPLE_PASSWORD_LENGTH - 1) { // 마지막 패스워드 숫자를 입력했을 경우
                    val innerLoginInfo = loginInfo ?: return@setOnClickNumberButton
                    safeApiRequest(
                        orderAPI.addOrder(
                            innerLoginInfo.getFormedToken(),
                            args.addOrderRequest.apply {
                                cardSimplePassword = adapter.getAllNumberText()
                            }
                        ),
                        onFail = { _, rawData ->
                            if (rawData == null) return@safeApiRequest
                            try {
                                val response =
                                    JACKSON_OBJECT_MAPPER.readValue(rawData, ErrorResponse::class.java)
                                showMessageDialog(response.message, showWarningImageView = true)
                            } catch (_: Throwable) {
                            }
                        }
                    ) { response ->
                        val serviceTypeId = args.addOrderRequest.serviceType
                        val deliveryTypeId = args.addOrderRequest.deliveryType
                        val basketShopList =
                            basketListViewModel.getSelectedBasketShopList(
                                serviceTypeId,
                                deliveryTypeId
                            )
                        basketShopList.forEach { basketListViewModel.removeSelectedMenu(it) }
                        val couponIdList =
                            args.addOrderRequest.orderShopList.flatMap { it.useCouponIdList }
                        innerLoginInfo.couponCount -= couponIdList.size
                        loginInfoViewModel.update()
                        val controller = findNavController()
                        val dest = controller.currentDestination ?: return@safeApiRequest
                        controller.popBackStack(R.id.basket_graph, true)
                        controller.navigate(
                            R.id.order_list_graph,
                            OrderListMainFragmentArgs(
                                serviceTypeId,
                                deliveryTypeId
                            ).toBundle(),
                            NavOptions.Builder().setPopUpTo(dest.id, false).build()
                        )
                        if (serviceTypeId != ServiceType.SHOPPING_MALL.id) {
                            controller.navigate(
                                R.id.order_status_graph,
                                OrderStatusFragmentArgs(response.orderId).toBundle(),
                                NavOptions.Builder().setPopUpTo(dest.id, false).build()
                            )
                        }
                    }
                }
            }
        }
    }
}