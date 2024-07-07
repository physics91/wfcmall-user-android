package com.theone.busandbt.adapter.coupon

import com.busandbt.code.DeliveryType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemCouponDeliveryTypeBinding
import com.theone.busandbt.extension.desc

class CouponDeliveryTypeListAdapter(items: List<DeliveryType> = emptyList()): DataBindingListAdapter<ItemCouponDeliveryTypeBinding, DeliveryType>() {

    override val viewHolderLayoutId: Int = R.layout.item_coupon_delivery_type

    init {
        _items.addAll(items)
    }

    override fun doBind(
        binding: ItemCouponDeliveryTypeBinding,
        item: DeliveryType,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            deliveryTypeTextView.text = item.desc()
        }
    }
}