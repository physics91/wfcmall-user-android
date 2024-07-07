package com.theone.busandbt.fragment.coupon

import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.coupon.MyCouponViewPagerAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.databinding.FragmentMyCouponBinding
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.utils.DELIVERY_TYPE_TAB_TITLE_ARRAY

/**
 * 내 쿠폰 목록 프레그먼트
 */
class MyCouponFragment : DataBindingFragment<FragmentMyCouponBinding>(), RequiredLogin,
    EnabledGoBackButton {

    override val layoutId: Int = R.layout.fragment_my_coupon
    override val actionBarTitle: String = "쿠폰"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            myCouponTabLayout.eventRegistrationTabSelectedDifferenceFont()
            myCouponViewPager.adapter = MyCouponViewPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(myCouponTabLayout, myCouponViewPager) { tab, position ->
                tab.text = DELIVERY_TYPE_TAB_TITLE_ARRAY[position]
            }.attach()

            goIssueCouponFragmentSpace.setOnClickListener {
                it.navigate(R.id.issue_coupon_graph)
            }
        }
    }
}