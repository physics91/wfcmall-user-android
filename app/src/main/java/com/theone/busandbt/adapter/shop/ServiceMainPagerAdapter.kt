package com.theone.busandbt.adapter.shop

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.theone.busandbt.fragment.shop.FoodMainInstantTabFragment
import com.theone.busandbt.fragment.shop.FoodMainPackagingTabFragment
import com.theone.busandbt.fragment.shop.ShoppingMainTabFragment
import com.theone.busandbt.utils.DELIVERY_TYPE_TAB_TITLE_ARRAY

/**
 * 탭 클릭시 변경되는 프래그먼트들을 연결하는 어뎁터
 * 음식점 , 포장주문 , 쇼핑몰  클릭 시 각각의 화면을 띄움
 */
class ServiceMainPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = DELIVERY_TYPE_TAB_TITLE_ARRAY.size

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return FoodMainInstantTabFragment()
            1 -> return FoodMainPackagingTabFragment()
            2 -> return ShoppingMainTabFragment()
        }
        return ShoppingMainTabFragment()
    }
}