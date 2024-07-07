package com.theone.busandbt.fragment.like

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.databinding.FragmentLikeListBinding
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.adapter.shop.LikeListPagerAdapter
import com.theone.busandbt.utils.DELIVERY_TYPE_TAB_TITLE_ARRAY
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.google.android.material.tabs.TabLayoutMediator

/***
 * 찜 메인 화면
 * 아래에 배달주문, 포장주문, 전통시장 탭이 있음
 */
class LikeListFragment : DataBindingFragment<FragmentLikeListBinding>(), EnabledGoBackButton,
    RequiredLogin {
    override val layoutId: Int = R.layout.fragment_like_list
    override val actionBarTitle: String = "찜"
    private val args by navArgs<LikeListFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            likeViewPager.isUserInputEnabled = false
            likeTabLayout.eventRegistrationTabSelectedDifferenceFont()
            likeViewPager.adapter = LikeListPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(likeTabLayout, likeViewPager) { tab, position ->
                tab.text = DELIVERY_TYPE_TAB_TITLE_ARRAY[position]
            }.attach()
            likeViewPager.post { tabMove() }
        }
    }

    private fun tabMove() {
        with(binding.likeViewPager) {
            when (args.serviceTypeId) {
                ServiceType.FOOD_DELIVERY.id -> {
                    when (args.deliveryTypeId) {
                        DeliveryType.INSTANT.id -> setCurrentItem(0, false)
                        DeliveryType.PACKAGING.id -> setCurrentItem(1, false)
                    }
                }
                ServiceType.SHOPPING_MALL.id -> {
                    when (args.deliveryTypeId) {
                        DeliveryType.PARCEL.id -> setCurrentItem(2, false)
                    }
                }
                else -> {}
            }
        }
    }
}