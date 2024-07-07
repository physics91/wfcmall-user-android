package com.theone.busandbt.adapter.menu

import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemMenuOptionGroupBinding
import com.theone.busandbt.dto.menu.MenuOptionGroup


/**
 * 메뉴 사이즈 어뎁터
 * */
class MenuOptionGroupAdapter(menuOptionGroupList: List<MenuOptionGroup> = emptyList()) :
    DataBindingListAdapter<ItemMenuOptionGroupBinding, MenuOptionGroup>() {
    override val viewHolderLayoutId: Int = R.layout.item_menu_option_group

    init {
        _items.addAll(menuOptionGroupList)
    }

    override fun doBind(
        binding: ItemMenuOptionGroupBinding,
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