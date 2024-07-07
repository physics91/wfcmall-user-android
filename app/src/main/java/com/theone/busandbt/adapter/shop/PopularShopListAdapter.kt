package com.theone.busandbt.adapter.shop

import androidx.navigation.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemPopularRestaurantBinding
import com.theone.busandbt.dto.shop.PopularShopListItem
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.fragment.shop.ServiceMainFragmentDirections
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType

/**
 * 인기매장 어댑터
 */
class PopularShopListAdapter(items: List<PopularShopListItem>) :
    DataBindingListAdapter<ItemPopularRestaurantBinding, PopularShopListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_popular_restaurant

    init {
        _items.addAll(items)
    }

    override fun doBind(
        binding: ItemPopularRestaurantBinding,
        item: PopularShopListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            popularShop = item
            popularImageView.clipToOutline = true
            popularRankingTextView.text = (_items.indexOf(item) + 1).toString()
            setOnItemClick { view, _, item ->
                val action =
                    ServiceMainFragmentDirections.actionRestaurantMainFragmentToShopDetailsFragment(
                        item.id,
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.INSTANT.id
                    )
                view.findNavController().navigate(action)
            }
        }
    }
}