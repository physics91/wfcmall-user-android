package com.theone.busandbt.adapter.menu

import android.annotation.SuppressLint
import android.view.MotionEvent
import com.blankj.utilcode.util.StringUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemMenuOptionBinding
import com.theone.busandbt.dto.menu.MenuOption
import com.theone.busandbt.eventbus.RefreshMenuOptionEvent
import com.theone.busandbt.extension.signText
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.busandbt.code.MenuStatus
import org.greenrobot.eventbus.EventBus

class MenuOptionAdapter(
    private val maxChoiceCount: Int,
    items: List<MenuOption>,
    private val requiredChoice: Boolean
) : DataBindingListAdapter<ItemMenuOptionBinding, MenuOption>() {
    override val viewHolderLayoutId: Int = R.layout.item_menu_option
    private var selectedPosition: Int = -1

    init {
        _items.addAll(items)
        if (requiredChoice) {
            if (items.isNotEmpty() && items.find { it.isSelected } == null) items.first().isSelected = true
            selectedPosition = items.indexOfFirst { it.isSelected }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun doBind(
        binding: ItemMenuOptionBinding,
        item: MenuOption,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            optionCheckbox.setBackgroundResource(if (requiredChoice) R.drawable.bg_shopping_basket_checkbox else R.drawable.bg_square_check_selector)
            val flag = item.status == MenuStatus.SELLING.id
            optionCheckbox.isEnabled = flag
            optionNameTextView.isSelected = flag
            optionCostTextView.isSelected = flag
            optionCheckbox.isChecked = item.isSelected
            optionNameTextView.text = item.name
            optionCostTextView.text =
                "${item.cost.signText()}${
                    StringUtils.getString(
                        R.string.commonCost,
                        item.cost.toMoneyFormat()
                    )
                }"
            if (requiredChoice) {
                optionCheckbox.setOnTouchListener { _, event ->
                    if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false
                    if (selectedPosition == position) return@setOnTouchListener true
                    if (!optionCheckbox.isChecked) {
                        val oldPosition = selectedPosition
                        if (oldPosition != -1) {
                            items[oldPosition].isSelected = false
                            notifyItemChanged(oldPosition)
                        }
                        item.isSelected = true
                        selectedPosition = position
                        notifyItemChanged(position)
                        EventBus.getDefault().post(RefreshMenuOptionEvent())
                    }
                    false
                }
            } else {
                optionCheckbox.setOnTouchListener { _, event ->
                    if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false
                    if (maxChoiceCount <= 0) {
                        item.isSelected = !optionCheckbox.isChecked
                        notifyItemChanged(position)
                        EventBus.getDefault().post(RefreshMenuOptionEvent())
                        return@setOnTouchListener false
                    }
                    if (!optionCheckbox.isChecked) {
                        val selectedCount = _items.filter { it.isSelected }.size
                        if (selectedCount >= maxChoiceCount) return@setOnTouchListener true
                    }
                    item.isSelected = !optionCheckbox.isChecked
                    notifyItemChanged(position)
                    EventBus.getDefault().post(RefreshMenuOptionEvent())
                    false
                }
            }
        }
    }
}