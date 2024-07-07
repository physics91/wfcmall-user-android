package com.theone.busandbt.adapter.shop

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemShopInfoCostBinding
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.item.ShopInfoCost

/**
 * 매장정보 할증료 정보 아이템
 */
class ShopInfoCostAdapter(costList: List<ShopInfoCost>): DataBindingListAdapter<ItemShopInfoCostBinding, ShopInfoCost>() {
    override val viewHolderLayoutId: Int = R.layout.item_shop_info_cost

    init {
        _items.addAll(costList)
    }

    override fun doBind(
        binding: ItemShopInfoCostBinding,
        item: ShopInfoCost,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            shopInfoCost = item
        }
    }
}