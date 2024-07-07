package com.theone.busandbt.fragment.onboarding

import android.os.Bundle
import android.text.Spannable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentOnboardingAppInformationBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.spanned.TypefaceSpanCompat

/**
 * 앱 최초 접속 - 온보딩 앱정보 소개 화면
 */
class OnboardingAppInformationFragment : DataBindingFragment<FragmentOnboardingAppInformationBinding>() {
    override val layoutId: Int = R.layout.fragment_onboarding_app_information

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.onboardingTextInclude) {
            titleTextView.text = buildSpannedString {
                append("부산의 필수앱\n")
                append(
                    "내 손안",
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(requireContext(), R.font.rixgoeb) ?: return@with
                    ),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("에 부산")
            }
            contentTextView.text = "앱 하나로 편리하게!\n모든 플랫폼을 담아내다"
        }
    }
}