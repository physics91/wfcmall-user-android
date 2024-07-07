package com.theone.busandbt.fragment.onboarding

import android.os.Bundle
import android.text.Spannable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentOnboardingDongbaektongBenefitBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.spanned.TypefaceSpanCompat

/**
 * 앱 최초 접속 - 온보딩 혜택 소개 화면
 */
class OnboardingDongbaektongBenefitFragment : DataBindingFragment<FragmentOnboardingDongbaektongBenefitBinding>() {
    override val layoutId: Int = R.layout.fragment_onboarding_dongbaektong_benefit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.onboardingTextInclude) {
            val ctx = requireContext()
            titleTextView.text = buildSpannedString {
                append("소비자는 ")
                append(
                    "할인혜택\n",
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(ctx, R.font.rixgoeb) ?: return@with
                    ),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("소상공인은 ")
                append(
                    "매출증대",
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(ctx, R.font.rixgoeb) ?: return@with
                    ),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            contentTextView.text = "동백전 온라인 결제 시 5% 추가 캐시백\n다양한 할인 이벤트까지!"
        }
    }
}