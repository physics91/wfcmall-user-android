package com.theone.busandbt.adapter.coupon

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.fragment.coupon.MyCouponFragmentArgs
import com.theone.busandbt.fragment.coupon.MyCouponListFragment
import com.theone.busandbt.utils.DELIVERY_TYPE_TAB_TITLE_ARRAY

/**
 * TODO 탭 순서, 위치, 개수를 좀 더 명확하게 지정할 것
 */
class MyCouponViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = DELIVERY_TYPE_TAB_TITLE_ARRAY.size

    override fun createFragment(position: Int): Fragment = when(position) {
        0 -> fragmentWithArgs(ServiceType.FOOD_DELIVERY, DeliveryType.INSTANT)
        1 -> fragmentWithArgs(ServiceType.FOOD_DELIVERY, DeliveryType.PACKAGING)
        2 -> fragmentWithArgs(ServiceType.SHOPPING_MALL, DeliveryType.PARCEL)
        else -> fragmentWithArgs(ServiceType.FOOD_DELIVERY, DeliveryType.INSTANT)
    }

    private fun fragmentWithArgs(serviceType: ServiceType, deliveryType: DeliveryType) = MyCouponListFragment().apply {
        arguments = MyCouponFragmentArgs(serviceType.id, deliveryType.id).toBundle()
    }
}
