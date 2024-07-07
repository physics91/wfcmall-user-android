package com.theone.busandbt.adapter.order

import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemShoppingDetailProductBinding
import com.theone.busandbt.dto.order.OrderMenuInDetail
import com.theone.busandbt.extension.calculateOptionCost
import com.theone.busandbt.extension.signText
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.adapter.DataBindingListAdapter

class OrderDetailMallMenuListAdapter(
    orderMenuList: List<OrderMenuInDetail>
) :
    DataBindingListAdapter<ItemShoppingDetailProductBinding, OrderMenuInDetail>() {
    override val viewHolderLayoutId: Int = R.layout.item_shopping_detail_product

    init {
        this._items.addAll(orderMenuList)
    }

    override fun doBind(
        binding: ItemShoppingDetailProductBinding,
        item: OrderMenuInDetail,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menu = item
            menuImageView.clipToOutline = true
            menuNameTextView.text = buildString {
                append(item.menuName)
                if (item.count > 1) append(" ${item.count}개")
            }
            CommonBindingAdapter.commonCost(
                menuCostTextView,
                (item.count * (item.cost + item.optionList.calculateOptionCost()))
            )
            optionTextView.text = buildString {
                append("ㆍ가격: ")
                val menuCostMoneyForm = item.cost.toCommonMoneyForm()
                if (item.menuCostName.isNotEmpty()) append("${item.menuCostName}($menuCostMoneyForm)") else append(menuCostMoneyForm)
                item.optionList.forEach { option ->
                    append("\nㆍ${option.groupName}: ${option.name}")
                    if (option.cost != 0) append("(${option.cost.signText()}${option.cost.toMoneyFormat()}원)")
                }
                if (isNotEmpty()) deleteCharAt(length - 1)
            }
        }
    }
}