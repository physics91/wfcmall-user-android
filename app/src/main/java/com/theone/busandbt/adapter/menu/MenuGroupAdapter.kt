package com.theone.busandbt.adapter.menu

import androidx.core.view.isVisible
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemMenuGroupBinding
import com.theone.busandbt.dto.menu.MenuGroup
import com.theone.busandbt.eventbus.SelectMenuEvent
import com.theone.busandbt.extension.getEmptyMenuImageResourceMap
import org.greenrobot.eventbus.EventBus

/**
 * 가게상세 메뉴아이템 중첩리사이클 바깥쪽 아이템
 */
class MenuGroupAdapter(
    menuGroupList: List<MenuGroup>
) : DataBindingListAdapter<ItemMenuGroupBinding, MenuGroup>() {
    override val viewHolderLayoutId: Int = R.layout.item_menu_group
    private val emptyMap = menuGroupList.getEmptyMenuImageResourceMap()

    init {
        _items.addAll(menuGroupList)
    }

    override fun doBind(
        binding: ItemMenuGroupBinding,
        item: MenuGroup,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menuGroup = item
            recyclerView.adapter = MenuAdapter(
                item.menuList,
                emptyMap[position] ?: emptyMap()
            ).apply {
                setOnItemClick { _, _, item ->
                    EventBus.getDefault().post(SelectMenuEvent(item.id))
                }
            }
            arrow.setOnClickListener {
                frameLayout.isVisible = !frameLayout.isVisible
                arrow.isSelected = !arrow.isSelected
            }
        }
    }
}