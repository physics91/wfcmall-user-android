package com.theone.busandbt.adapter.shop

import android.content.res.ColorStateList
import com.blankj.utilcode.util.ColorUtils
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemOperBinding
import com.theone.busandbt.item.shop.OperViewMetadata

class OperListAdapter(items: List<OperViewMetadata> = emptyList()) :
    DataBindingListAdapter<ItemOperBinding, OperViewMetadata>() {
    override val viewHolderLayoutId: Int = R.layout.item_oper

    init {
        _items.addAll(items)
    }

    override fun doBind(
        binding: ItemOperBinding,
        item: OperViewMetadata,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            this.item = item
            operTitleTextView.backgroundTintList =
                ColorStateList.valueOf(ColorUtils.getColor(item.type.badgeColorResId))
        }
    }
}