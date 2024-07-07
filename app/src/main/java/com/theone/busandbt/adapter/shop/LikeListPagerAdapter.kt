package com.theone.busandbt.adapter.shop

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.fragment.like.FoodLikeListFragment
import com.theone.busandbt.fragment.like.ShoppingLikeListFragment

/**
 * TODO - 서비스, 배달유형을 하위 화면에 넘길것
 */
class LikeListPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabs = arrayOf(
        FoodLikeListFragment().apply {
            arguments = bundleOf(
                "serviceTypeId" to ServiceType.FOOD_DELIVERY.id,
                "deliveryTypeId" to DeliveryType.INSTANT.id
            )
        },
        FoodLikeListFragment().apply {
            arguments = bundleOf(
                "serviceTypeId" to ServiceType.FOOD_DELIVERY.id,
                "deliveryTypeId" to DeliveryType.PACKAGING.id
            )
        },
        ShoppingLikeListFragment().apply {
            arguments = bundleOf(
                "serviceTypeId" to ServiceType.SHOPPING_MALL.id,
                "deliveryTypeId" to DeliveryType.PARCEL.id
            )
        },
    )

    override fun getItemCount(): Int {
        return tabs.size
    }

    override fun createFragment(position: Int): Fragment {
        return tabs[position]
    }
}