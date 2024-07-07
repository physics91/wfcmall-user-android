package com.theone.busandbt.adapter.order

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemShoppingOrderBinding
import com.theone.busandbt.dto.order.MallOrderListItem
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.fragment.order.OrderDetailFragmentArgs

/**
 * 주문내역 쇼핑몰 어뎁터
 * TODO: 현재 포장주문 데이터들이 들어가 있음 추후 변경 해야함
 */
class MallOrderListAdapter :
    DataBindingListAdapter<ItemShoppingOrderBinding, MallOrderListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_shopping_order
    override fun doBind(
        binding: ItemShoppingOrderBinding,
        item: MallOrderListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            order = item
            goToOrderDetailButton.setOnClickListener {
                it.navigate(
                    R.id.shopping_order_detail_graph,
                    OrderDetailFragmentArgs(item.id).toBundle()
                )
            }
        }
    }
}