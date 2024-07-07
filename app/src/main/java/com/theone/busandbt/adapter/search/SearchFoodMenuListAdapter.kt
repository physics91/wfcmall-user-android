package com.theone.busandbt.adapter.search

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemSearchFoodMenuBinding
import com.theone.busandbt.dto.menu.FoodMenuListItem
import com.theone.busandbt.adapter.DataBindingListAdapter

class SearchFoodMenuListAdapter(menuList: List<FoodMenuListItem> = emptyList()) :
    DataBindingListAdapter<ItemSearchFoodMenuBinding, FoodMenuListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_search_food_menu

    init {
        _items.addAll(menuList)
    }

    override fun doBind(
        binding: ItemSearchFoodMenuBinding,
        item: FoodMenuListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menu = item
            menuImageView.clipToOutline = true
        }
    }
}