package com.theone.busandbt.adapter.order

import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemOrderDetailMenuBinding
import com.theone.busandbt.dto.order.OrderMenuInDetail
import com.theone.busandbt.extension.signText
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.adapter.DataBindingListAdapter

/**
 * 주문상세 가격 어뎁터
 */
class OrderDetailMenuListAdapter(orderMenuDetailList: List<OrderMenuInDetail>) :
    DataBindingListAdapter<ItemOrderDetailMenuBinding, OrderMenuInDetail>() {

    init {
        _items.addAll(orderMenuDetailList)
    }

    override val viewHolderLayoutId: Int = R.layout.item_order_detail_menu

    override fun doBind(
        binding: ItemOrderDetailMenuBinding,
        item: OrderMenuInDetail,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            orderMenuDetail = item
            menuNameTextView.text = buildString {
                append(item.menuName)
                if (item.count > 1) append(" ${item.count}개")
            }
            CommonBindingAdapter.commonCost(
                totalMenuCostTextView,
                item.count * (item.cost + item.optionList.sumOf { option -> option.cost })
            )

            val items = ArrayList<Pair<String, String>>()
            val menuCostText = buildString {
                val menuCostMoneyForm = item.cost.toCommonMoneyForm()
                if (item.menuCostName.isNotEmpty()) append("${item.menuCostName}($menuCostMoneyForm)") else append(menuCostMoneyForm)
            }
            items.add("가격" to menuCostText)
            items.addAll(item.optionList.groupBy { it.groupName }.map { (t, u) ->
                t to u.joinToString(",") { optionItem ->
                    "${optionItem.name}(${optionItem.cost.signText()}${optionItem.cost.toCommonMoneyForm()})"
                }
            })
            menuOptionRecyclerView.adapter = OrderDetailMenuOptionListAdapter(items)
        }
    }
}