package com.theone.busandbt.adapter.menu

import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemMallMenuInfoBinding
import com.theone.busandbt.dto.menu.MenuInfo


/**
 * 쇼핑몰 상품정보제공고시
 */
class MallMenuInfoListAdapter(menuInfoList: List<MenuInfo>?) :
    DataBindingListAdapter<ItemMallMenuInfoBinding, MenuInfo>() {
    override val viewHolderLayoutId: Int = R.layout.item_mall_menu_info

    init {
        menuInfoList?.let { _items.addAll(it) }
    }

    override fun doBind(
        binding: ItemMallMenuInfoBinding,
        item: MenuInfo,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menuItem = item
        }
    }
}