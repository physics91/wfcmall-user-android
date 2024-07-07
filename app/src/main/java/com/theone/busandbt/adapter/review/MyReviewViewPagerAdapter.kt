package com.theone.busandbt.adapter.review

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.theone.busandbt.fragment.review.MyReviewFragmentArgs
import com.theone.busandbt.fragment.review.MyReviewListFragment
import com.theone.busandbt.utils.DELIVERY_TYPE_TAB_TITLE_ARRAY
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType

class MyReviewViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = DELIVERY_TYPE_TAB_TITLE_ARRAY.size

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> fragmentWithArgs(ServiceType.FOOD_DELIVERY, DeliveryType.INSTANT)
        1 -> fragmentWithArgs(ServiceType.FOOD_DELIVERY, DeliveryType.PACKAGING)
        2 ->fragmentWithArgs(ServiceType.SHOPPING_MALL, DeliveryType.PARCEL)
        else ->fragmentWithArgs(ServiceType.FOOD_DELIVERY, DeliveryType.INSTANT)
    }

    private fun fragmentWithArgs(serviceType: ServiceType, deliveryType: DeliveryType) =
        MyReviewListFragment().apply {
            arguments = MyReviewFragmentArgs(serviceType.id, deliveryType.id).toBundle()
        }
}