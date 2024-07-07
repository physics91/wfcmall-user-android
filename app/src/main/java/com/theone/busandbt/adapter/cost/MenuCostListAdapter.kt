package com.theone.busandbt.adapter.cost

import android.annotation.SuppressLint
import android.view.MotionEvent
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemMenuCostBinding
import com.theone.busandbt.dto.cost.MenuCostListItem
import com.theone.busandbt.eventbus.SelectMenuCostEvent
import com.theone.busandbt.adapter.DataBindingListAdapter
import org.greenrobot.eventbus.EventBus

class MenuCostListAdapter(
    items: List<MenuCostListItem>
) : DataBindingListAdapter<ItemMenuCostBinding, MenuCostListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_menu_cost
    private var selectedPosition: Int = -1

    init {
        _items.addAll(items)
        selectedPosition = items.indexOfFirst { it.isSelected }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun doBind(
        binding: ItemMenuCostBinding,
        item: MenuCostListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menuCostCheckbox.setBackgroundResource(R.drawable.bg_shopping_basket_checkbox)
            val flag = true
            menuCostCheckbox.isEnabled = flag
            menuCostNameTextView.isSelected = flag
            menuCostTextView.isSelected = flag
            menuCostCheckbox.isChecked = item.isSelected
            menuCostNameTextView.text = item.name
            CommonBindingAdapter.commonCost(menuCostTextView, item.saleCost)
            menuCostCheckbox.setOnTouchListener { _, event ->
                if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false
                if (menuCostCheckbox.isChecked) return@setOnTouchListener false
                if (selectedPosition < 0) return@setOnTouchListener false
                val oldPosition = selectedPosition
                val old = items[selectedPosition]
                old.isSelected = false
                item.isSelected = true
                notifyItemChanged(oldPosition)
                notifyItemChanged(position)
                selectedPosition = position
                EventBus.getDefault().post(SelectMenuCostEvent(item))
                false
            }
        }
    }
}