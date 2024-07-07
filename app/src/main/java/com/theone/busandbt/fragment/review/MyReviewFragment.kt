package com.theone.busandbt.fragment.review

import android.os.Bundle
import android.view.View
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentMyReviewBinding
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.extension.reduceDragSensitivity
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.adapter.review.MyReviewViewPagerAdapter
import com.theone.busandbt.utils.DELIVERY_TYPE_TAB_TITLE_ARRAY
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 내 리뷰 관리 프레그먼트
 */
class MyReviewFragment : DataBindingFragment<FragmentMyReviewBinding>(), EnabledGoBackButton {

    override val layoutId: Int = R.layout.fragment_my_review
    override val actionBarTitle: String = "내가 쓴 리뷰 관리"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            reviewTabLayout.eventRegistrationTabSelectedDifferenceFont()
            reviewViewPager.adapter = MyReviewViewPagerAdapter(childFragmentManager, lifecycle)
            reviewViewPager.reduceDragSensitivity()
            TabLayoutMediator(reviewTabLayout, reviewViewPager) { tab, position ->
                tab.text = DELIVERY_TYPE_TAB_TITLE_ARRAY[position]
            }.attach()
        }
    }
}