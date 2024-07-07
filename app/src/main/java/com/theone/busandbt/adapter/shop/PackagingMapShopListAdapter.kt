package com.theone.busandbt.adapter.shop

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemPackagingMapShopBinding
import com.theone.busandbt.dto.shop.PackagingShopInfo
import com.theone.busandbt.adapter.DataBindingListAdapter

class PackagingMapShopListAdapter :
    DataBindingListAdapter<ItemPackagingMapShopBinding, PackagingShopInfo>() {
    override val viewHolderLayoutId: Int = R.layout.item_packaging_map_shop

    override fun doBind(
        binding: ItemPackagingMapShopBinding,
        item: PackagingShopInfo,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            shop = item
            shopImageView.clipToOutline = true
        }
    }
}