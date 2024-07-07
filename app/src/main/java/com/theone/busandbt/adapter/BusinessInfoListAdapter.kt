package com.theone.busandbt.adapter

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemBusinessInfoBinding
import com.theone.busandbt.utils.BUSINESS_INFO_ARRAY
import com.theone.busandbt.dto.BusinessInfo

class BusinessInfoListAdapter : DataBindingListAdapter<ItemBusinessInfoBinding, BusinessInfo>() {

    init {
        _items.addAll(BUSINESS_INFO_ARRAY)
    }

    override val viewHolderLayoutId: Int = R.layout.item_business_info

    override fun doBind(
        binding: ItemBusinessInfoBinding,
        item: BusinessInfo,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            title = item.title
            content = item.content
        }
    }
}