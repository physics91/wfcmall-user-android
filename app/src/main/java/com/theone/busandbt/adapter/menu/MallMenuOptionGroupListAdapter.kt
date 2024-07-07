package com.theone.busandbt.adapter.menu

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemMallMenuOptionGroupBinding
import com.theone.busandbt.dto.menu.MenuOptionGroup
import com.theone.busandbt.adapter.DataBindingListAdapter


class MallMenuOptionGroupListAdapter(menuOptionGroupList: List<MenuOptionGroup> = emptyList()) :
    DataBindingListAdapter<ItemMallMenuOptionGroupBinding, MenuOptionGroup>() {
    override val viewHolderLayoutId: Int = R.layout.item_mall_menu_option_group

    init {
        _items.addAll(menuOptionGroupList)
    }

    override fun doBind(
        binding: ItemMallMenuOptionGroupBinding,
        item: MenuOptionGroup,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menuOptionGroup = item
            menuOptionName.text = buildString {
                append(item.name)
                if (item.maxChoiceCount > 1) append(" (최대 ${item.maxChoiceCount}개)")
            }
            menuOptionRecyclerView.adapter =
                MenuOptionAdapter(item.maxChoiceCount, item.optionList, item.requiredChoice)
        }
    }
}