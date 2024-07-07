package com.theone.busandbt.adapter.shop

import androidx.core.view.isVisible
import com.blankj.utilcode.util.ColorUtils
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemNewShopBinding
import com.theone.busandbt.dto.shop.NewShopListItem

/**
 * 신규오픈매장 모두보기 어댑터
 */
class NewShopListAdapter(items: List<NewShopListItem> = emptyList()) : DataBindingListAdapter<ItemNewShopBinding, NewShopListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_new_shop

    init {
        _items.addAll(items)
    }

    override fun doBind(
        binding: ItemNewShopBinding,
        item: NewShopListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            newShopListItem = item
            newLogoImageView.clipToOutline = true
            val deliveryCostCheck = item.minDeliveryCost <= 0
            tipTextView.text = if (deliveryCostCheck) "무료배달" else "배달비"
            tipTextView.setTextColor(ColorUtils.getColor(if (deliveryCostCheck) R.color.mainColor else R.color.foodMainItemDeliveryTipText))
            newDeliveryTextView.isVisible = !deliveryCostCheck
        }
    }
}