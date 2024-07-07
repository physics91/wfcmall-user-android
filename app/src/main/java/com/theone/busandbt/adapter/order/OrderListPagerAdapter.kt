package com.theone.busandbt.adapter.order

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.theone.busandbt.fragment.order.MallOrderListItemFragment
import com.theone.busandbt.fragment.order.OrderListItemFragment
import com.theone.busandbt.function.createTabArguments
import com.theone.busandbt.utils.DELIVERY_TYPE_TAB_TITLE_ARRAY


/**
 * 주문내역 탭 어뎁터
 */
class OrderListPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = DELIVERY_TYPE_TAB_TITLE_ARRAY.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            2 -> MallOrderListItemFragment().apply {
                arguments = bundleOf("position" to position)
            }

            else -> OrderListItemFragment().apply {
                arguments = createTabArguments(position).apply {
                    putInt("position", position)
                }
            }
        }
    }
}