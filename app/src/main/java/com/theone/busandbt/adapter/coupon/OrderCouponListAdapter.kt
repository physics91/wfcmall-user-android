package com.theone.busandbt.adapter.coupon

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemUseCouponBinding
import com.theone.busandbt.dto.order.OrderCoupon
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.adapter.DataBindingListAdapter

class OrderCouponListAdapter(items: List<OrderCoupon>) :
    DataBindingListAdapter<ItemUseCouponBinding, OrderCoupon>() {

    init {
        _items.addAll(items)
    }

    override val viewHolderLayoutId: Int = R.layout.item_use_coupon

    override fun doBind(
        binding: ItemUseCouponBinding,
        item: OrderCoupon,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            couponDiscountCostTextView.text = "(-)${item.couponDiscountCost.toCommonMoneyForm()}"
            couponNameTextView.text = item.couponName
        }
    }
}