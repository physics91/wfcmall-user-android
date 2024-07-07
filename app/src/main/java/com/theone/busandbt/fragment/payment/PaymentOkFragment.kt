package com.theone.busandbt.fragment.payment

import android.os.Bundle
import android.text.SpannedString
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentPaymentOkBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.spanned.TypefaceSpanCompat

class PaymentOkFragment : DataBindingFragment<FragmentPaymentOkBinding>() {
    override val layoutId: Int = R.layout.fragment_payment_ok

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
                    SpannedString.SPAN_EXCLUSIVE_INCLUSIVE
                )
                append("버튼을 눌러주세요.")
            }
        }
    }
}