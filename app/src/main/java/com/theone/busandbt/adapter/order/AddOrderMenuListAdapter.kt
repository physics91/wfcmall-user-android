package com.theone.busandbt.adapter.order

import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemAddOrderMenuListBinding
import com.theone.busandbt.extension.signText
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.item.basket.BasketMenu

/**
 * 주문하기  주문내역 어뎁터
 */
class AddOrderMenuListAdapter(menuList: List<BasketMenu>) :
    DataBindingListAdapter<ItemAddOrderMenuListBinding, BasketMenu>() {
    override val viewHolderLayoutId: Int = R.layout.item_add_order_menu_list

    init {
        _items.addAll(menuList)
    }

    override fun doBind(
        binding: ItemAddOrderMenuListBinding,
        item: BasketMenu,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menu = item
            menuNameTextView.text = buildString {
                append(item.name)
                if (item.count > 1) append(" ${item.count}개")
            }
            CommonBindingAdapter.commonCost(
                menuCostTextView,
                item.count * (item.saleCost + item.optionList.sumOf { option -> option.cost })
            )
            val items = ArrayList<Pair<String, String>>()
            val menuCostText = buildString {
                append("ㆍ")
                if (item.menuCostName.isNotEmpty()) append("${item.menuCostName} ")
                append("(${item.saleCost.toCommonMoneyForm()})")
            }
            items.add("가격" to menuCostText)
            items.addAll(item.optionList.groupBy { it.optionGroupName }.map { (t, u) ->
                t to u.joinToString("\n") { optionItem ->
                    "ㆍ${optionItem.name} (${optionItem.cost.signText()}${optionItem.cost.toCommonMoneyForm()})"
                }
            })
            menuOptionRecyclerView.adapter = AddOrderMenuOptionListAdapter(items)
        }
    }
}