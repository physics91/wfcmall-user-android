package com.theone.busandbt.adapter.search

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemAddressSearchBinding
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.dto.address.AddressSearchResultItem

/**
 * 주소 설정 페이지 주소검색 목록 어뎁터
 */
class SearchedAddressListAdapter :
    DataBindingListAdapter<ItemAddressSearchBinding, AddressSearchResultItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_address_search

    override fun doBind(
        binding: ItemAddressSearchBinding,
        item: AddressSearchResultItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            addressItem = item
        }
    }
}