package com.theone.busandbt.adapter.shop

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.theone.busandbt.dto.shop.ShopDetail
import com.theone.busandbt.fragment.shop.ShopDetailDeliveryFragment
import com.theone.busandbt.fragment.shop.ShopDetailPackagingFragment
import com.busandbt.code.DeliveryType

/**
 * 매장상세의 배달유형 탭 어댑터
 */
class ShopDetailsTabAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val shopId: Int,
    private val shopDetail: ShopDetail,
    private val deliveryTypeList: List<DeliveryType>,
    private val serviceTypeId: Int
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return deliveryTypeList.size
    }

    override fun createFragment(position: Int): Fragment {
        val args = bundleOf(
            "shopId" to shopId,
            "shopDetail" to shopDetail,
            "serviceTypeId" to serviceTypeId
        )
        return when (deliveryTypeList[position]) {
            DeliveryType.INSTANT -> ShopDetailDeliveryFragment().apply {
                arguments = args
            }
            DeliveryType.PACKAGING -> ShopDetailPackagingFragment().apply {
                arguments = args
            }
            else -> ShopDetailDeliveryFragment().apply {
                arguments = args
            }
        }
    }
}