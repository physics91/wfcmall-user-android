package com.theone.busandbt.fragment.onboarding

import android.os.Bundle
import android.text.Spannable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentOnboardingStartDongbaektongBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.spanned.TypefaceSpanCompat

/**
 * 앱 최초 접속 - 온보딩 동백통 시작하기 화면
 */
class OnboardingStartDongbaektong : DataBindingFragment<FragmentOnboardingStartDongbaektongBinding>(),
    View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_onboarding_start_dongbaektong

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            goToStartButton.setOnClickListener(this@OnboardingStartDongbaektong)

            onboardingTextInclude.titleTextView.text = buildSpannedString {
                append("특별한 공공배달앱\n")
                append(
                    "동백통",
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(requireContext(), R.font.rixgoeb) ?: return@with
                    ),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("을 만나보세요!")
            }
            onboardingTextInclude.contentTextView.text = "오늘 집에서 동백통과 함께 하실래요?"
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view) {
                goToStartButton -> findNavController().navigate(R.id.action_onboardingFragment_to_permissionAgreeFragment)
            }
        }
    }
}