package com.theone.busandbt.fragment.payment

import android.os.Bundle
import android.text.Spannable
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentPaymentFailBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.spanned.TypefaceSpanCompat

class PaymentFailFragment : DataBindingFragment<FragmentPaymentFailBinding>(), EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_payment_fail
    override val actionBarTitle: String = "결제 실패"
    private val args by navArgs<PaymentFailFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val ctx = requireContext()
            titleTextView.text = buildSpannedString {
                append("주문이 정상적으로\n")
                append(
                    "완료되지 않았습니다.",
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(ctx, R.font.sult_bold) ?: return@with
                    ),
                    SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            paymentFailReasonTextView.text = buildSpannedString {
                append("결제 실패 사유: ")
                append(args.resultMessage, ForegroundColorSpan(MAIN_COLOR), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            goBasketButton.setOnClickListener {
                findNavController().popBackStack(R.id.add_order_graph, true)
            }
        }
    }
}