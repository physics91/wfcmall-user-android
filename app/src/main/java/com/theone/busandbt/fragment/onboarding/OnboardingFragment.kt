package com.theone.busandbt.fragment.onboarding

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentOnboardingBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.adapter.OnBoardingPageAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 앱 최초 접속 - 온보딩 관리 프래그먼트
 */
class OnboardingFragment : DataBindingFragment<FragmentOnboardingBinding>() {

    override val layoutId: Int = R.layout.fragment_onboarding
    private var autoScrollJob: Job? = null

    /**
     * 동적으로 할당할 탭 리스트
     */
    private val tabs = listOf(
        OnboardingAppInformationFragment(),
        OnboardingDongbaektongBenefitFragment(),
        OnboardingStartDongbaektong()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            initWindow()
            onboardingViewPager.adapter =
                OnBoardingPageAdapter(childFragmentManager, lifecycle, tabs)
            onboardingViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (tabs.isEmpty()) return
                    val progress = (position + 1) * 100 / tabs.size  // 프로그래스바 진행률 설정
                    pageChangeProgressBar.progress = progress
                    restartAutoScroll()
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        autoScrollJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        startAutoScroll()
    }

    private fun startAutoScroll() {
        autoScrollJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(3000) // 3초 대기
                val currentItem = binding.onboardingViewPager.currentItem
                val nextItem = if (currentItem < tabs.size - 1) currentItem + 1 else 0
                if (nextItem > 0) binding.onboardingViewPager.setCurrentItem(nextItem, true)
            }
        }
    }

    private fun restartAutoScroll() {
        autoScrollJob?.cancel()
        startAutoScroll()
    }

    private fun initWindow() {
        val window: Window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
    }
}