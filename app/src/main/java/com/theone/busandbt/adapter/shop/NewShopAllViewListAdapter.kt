package com.theone.busandbt.adapter.shop

import androidx.core.view.isVisible
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemNewShopViewAllBinding
import com.theone.busandbt.dto.shop.NewShopListItem
import com.theone.busandbt.adapter.DataBindingListAdapter

class NewShopAllViewListAdapter : DataBindingListAdapter<ItemNewShopViewAllBinding, NewShopListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_new_shop_view_all

    override fun doBind(
        binding: ItemNewShopViewAllBinding,
        item: NewShopListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            newShopListItem = item
            newLogoImageView.clipToOutline = true
            val deliveryCostCheck = item.minDeliveryCost <= 0
            tipTextView.text = if (deliveryCostCheck) "무료배달" else "배달비"
            newDeliveryTextView.isVisible = !deliveryCostCheck
        }
    }
}