package com.theone.busandbt.adapter.search

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.theone.busandbt.fragment.search.SearchFoodMenuResultFragment
import com.theone.busandbt.fragment.search.SearchShoppingResultFragment
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType

/**
 * 상점 상세 검색 결과에서 쇼핑몰 검색인지 아닌지 구분
 */
class SearchDetailResultAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val shopId: Int,
    private val shopName: String,
    private val minOrderCost: Int,
    private val deliveryTypeId: Int,
) : FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun getItemCount(): Int {
        return 1
    }

    override fun createFragment(position: Int): Fragment {
        return when (deliveryTypeId) {
            DeliveryType.PARCEL.id -> SearchShoppingResultFragment().apply {
                arguments = bundleOf(
                    "shopId" to shopId,
                    "serviceTypeId" to ServiceType.SHOPPING_MALL.id,
                    "deliveryTypeId" to deliveryTypeId
                )
            }
            else -> SearchFoodMenuResultFragment().apply {
                arguments = bundleOf(
                    "shopId" to shopId,
                    "shopName" to shopName,
                    "minOrderCost" to minOrderCost,
                    "serviceTypeId" to ServiceType.FOOD_DELIVERY.id,
                    "deliveryTypeId" to deliveryTypeId
                )
            }
        }
    }
}