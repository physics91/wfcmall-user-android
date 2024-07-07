package com.theone.busandbt.adapter.order

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemOrderDetailMenuOptionBinding
import com.theone.busandbt.adapter.DataBindingListAdapter

class OrderDetailMenuOptionListAdapter(items: List<Pair<String, String>>) :
    DataBindingListAdapter<ItemOrderDetailMenuOptionBinding, Pair<String, String>>() {
    init {
        _items.addAll(items)
    }

    override val viewHolderLayoutId: Int = R.layout.item_order_detail_menu_option

    override fun doBind(
        binding: ItemOrderDetailMenuOptionBinding,
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