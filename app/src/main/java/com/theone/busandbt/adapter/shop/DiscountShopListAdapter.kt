package com.theone.busandbt.adapter.shop

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemDiscountShopBinding
import com.theone.busandbt.dto.shop.DiscountShopListItem
import com.theone.busandbt.adapter.DataBindingListAdapter

/**
 * 할인 매장 리스트 어댑터입니다.
 */
class DiscountShopListAdapter(items: List<DiscountShopListItem> = emptyList()) :
    DataBindingListAdapter<ItemDiscountShopBinding, DiscountShopListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_discount_shop

    init {
        _items.addAll(items)
    }

    override fun doBind(
        binding: ItemDiscountShopBinding,
        item: DiscountShopListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            discountShop = item
            logoImageView.clipToOutline = true
        }
    }
}