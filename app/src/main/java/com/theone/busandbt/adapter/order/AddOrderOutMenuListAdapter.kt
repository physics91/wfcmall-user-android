package com.theone.busandbt.adapter.order

import androidx.core.view.isVisible
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemAddOrderOutMenuListBinding
import com.theone.busandbt.extension.getSelectedMenuList
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.item.basket.BasketShop
import com.busandbt.code.ServiceType

class AddOrderOutMenuListAdapter(
    selectedBasketShopList: List<BasketShop>,
    private val inputServiceType: ServiceType
) :
    DataBindingListAdapter<ItemAddOrderOutMenuListBinding, BasketShop>() {
    override val viewHolderLayoutId: Int = R.layout.item_add_order_out_menu_list
    private val addOrderListAdapter =
        HashMap<Int, AddOrderMenuListAdapter>()

    init {
        _items.addAll(selectedBasketShopList)
    }

    override fun doBind(
        binding: ItemAddOrderOutMenuListBinding,
        item: BasketShop,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            shop = item
            serviceType = inputServiceType
            downArrowImageView.setOnCheckedChangeListener { _, isChecked ->
                menuRecyclerView.isVisible = isChecked
                downArrowImageView.isChecked = isChecked
            }
            menuRecyclerView.adapter =
                addOrderListAdapter.getOrPut(item.shopId) {
                    AddOrderMenuListAdapter(
                        item.getSelectedMenuList()
                    )
                }
        }
    }
}