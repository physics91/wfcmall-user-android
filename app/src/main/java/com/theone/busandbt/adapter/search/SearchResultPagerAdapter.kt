package com.theone.busandbt.adapter.search

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.adapter.StoredPagerAdapter
import com.theone.busandbt.fragment.search.SearchFoodShopResultFragment
import com.theone.busandbt.fragment.search.SearchShoppingResultFragment
import com.theone.busandbt.fragment.search.SearchTotalResultFragment

/**
 * 검색 결과에서 탭 클릭시 변경되는 프래그먼트들을 연결하는 어뎁터
 * 전체, 배달주문, 포장주문, 전통시장 으로 이루어져있다.
 */
class SearchResultPagerAdapter(
    override val fragment: Fragment,
    private val tabTitleArray: Array<String>
) : StoredPagerAdapter(fragment) {

    override fun getItemCount(): Int = tabTitleArray.size

    override fun defineFragment(position: Int): Fragment = when (position) {
        0 -> SearchTotalResultFragment().apply {
            arguments = bundleOf("position" to position)
        }

        1 -> SearchFoodShopResultFragment().apply {
            arguments = bundleOf(
                "serviceTypeId" to ServiceType.FOOD_DELIVERY.id,
                "deliveryTypeId" to DeliveryType.INSTANT.id,
                "position" to position
            )
        }

        2 -> SearchFoodShopResultFragment().apply {
            arguments = bundleOf(
                "serviceTypeId" to ServiceType.FOOD_DELIVERY.id,
                "deliveryTypeId" to DeliveryType.PACKAGING.id,
                "position" to position
            )
        }

        3 -> SearchShoppingResultFragment().apply {
            arguments = bundleOf(
                "serviceTypeId" to ServiceType.SHOPPING_MALL.id,
                "deliveryTypeId" to DeliveryType.PARCEL.id,
                "position" to position
            )
        }

        else -> error("코드를 잘짜야지! 허용된 범위를 벗어났잖아!")
    }
}