package com.theone.busandbt.adapter.basket

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.theone.busandbt.fragment.basket.BasketDeliveryTypeFragment
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType

/**
 * 장바구니 탭 어뎁터
 */
class BasketViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val pagerServiceAndDeliveryTypeList = arrayOf(
        ServiceType.FOOD_DELIVERY to DeliveryType.INSTANT,
        ServiceType.FOOD_DELIVERY to DeliveryType.PACKAGING,
        ServiceType.SHOPPING_MALL to DeliveryType.PARCEL
    )

    override fun getItemCount(): Int = pagerServiceAndDeliveryTypeList.size

    override fun createFragment(position: Int): Fragment {
        val item = pagerServiceAndDeliveryTypeList[position]
        return when {
            else -> BasketDeliveryTypeFragment().apply {
                arguments = bundleOf(
                    "serviceTypeId" to item.first.id,
                    "deliveryTypeId" to item.second.id
                )
            }
        }
    }
}