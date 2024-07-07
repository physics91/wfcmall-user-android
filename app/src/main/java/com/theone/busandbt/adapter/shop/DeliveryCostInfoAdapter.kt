package com.theone.busandbt.adapter.shop

import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemDeliveryCostInfoBinding
import com.theone.busandbt.dto.cost.DeliveryCost
import com.theone.busandbt.extension.toCommonMoneyForm

class DeliveryCostInfoAdapter(deliveryCostList: List<DeliveryCost>) : DataBindingListAdapter<ItemDeliveryCostInfoBinding, DeliveryCost>() {

    override val viewHolderLayoutId: Int = R.layout.item_delivery_cost_info

    init {
        _items.addAll(deliveryCostList.sortedByDescending { it.testOrderCost })
    }

    override fun doBind(
        binding: ItemDeliveryCostInfoBinding,
        item: DeliveryCost,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            val minOrderCost = when (position) {
                0 -> -1
                else -> items[position - 1].testOrderCost
            }
            standardTextView.text = if (minOrderCost == -1) "${item.testOrderCost.toCommonMoneyForm()} 이상" else "${item.testOrderCost.toCommonMoneyForm()} 이상 ~ ${minOrderCost.toCommonMoneyForm()} 미만"
            deliveryCostTextView.text = item.cost.toCommonMoneyForm()
        }
    }
}