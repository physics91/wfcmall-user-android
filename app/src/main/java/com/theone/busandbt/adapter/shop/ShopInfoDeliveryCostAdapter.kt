package com.theone.busandbt.adapter.shop

import com.blankj.utilcode.util.ResourceUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemShopInfoDeliveryCostBinding
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.item.ShopInfoDeliveryCost
import com.theone.busandbt.view.recyclerview.decoration.EasyDividerDecoration
import com.busandbt.code.DeliveryType

class ShopInfoDeliveryCostAdapter(
    list: List<ShopInfoDeliveryCost>,
    private val deliveryType: List<DeliveryType>
) :
    DataBindingListAdapter<ItemShopInfoDeliveryCostBinding, ShopInfoDeliveryCost>() {
    override val viewHolderLayoutId: Int = R.layout.item_shop_info_delivery_cost

    init {
        _items.addAll(list)
    }

    override fun doBind(
        binding: ItemShopInfoDeliveryCostBinding,
        item: ShopInfoDeliveryCost,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            deliveryCostInfoTitle.text = item.title
            val shoppingCheck =
                deliveryType.any { it == DeliveryType.PARCEL }  //TODO: 임시로 딜리버리 타입을 PARCEL 로 해놨는데 변경 해야함
            if (shoppingCheck) deliveryCostInfoTitle.text = "배송비"
            deliveryCostInfoRecyclerView.addItemDecoration(
                EasyDividerDecoration(
                    ResourceUtils.getDrawable(
                        R.drawable.divider_delivery_cost
                    )
                )
            )
            deliveryCostInfoRecyclerView.adapter = ShopInfoCostAdapter(item.shopInfoCostList)
        }
    }
}