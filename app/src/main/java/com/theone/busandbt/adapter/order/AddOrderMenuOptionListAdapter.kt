package com.theone.busandbt.adapter.order

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemAddOrderMenuOptionBinding
import com.theone.busandbt.adapter.DataBindingListAdapter

class AddOrderMenuOptionListAdapter(items: List<Pair<String, String>>) :
    DataBindingListAdapter<ItemAddOrderMenuOptionBinding, Pair<String, String>>() {
    init {
        _items.addAll(items)
    }

    override val viewHolderLayoutId: Int = R.layout.item_add_order_menu_option

    override fun doBind(
        binding: ItemAddOrderMenuOptionBinding,
        item: Pair<String, String>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menuOptionTitleTextView.text = item.first
            menuOptionContentTextView.text = item.second
        }
    }
}