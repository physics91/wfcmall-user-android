package com.theone.busandbt.adapter

import android.view.View
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemSelectionBinding
import com.theone.busandbt.extension.sultBold
import com.theone.busandbt.extension.sultMedium
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.instance.MAIN_TEXT_COLOR
import com.theone.busandbt.utils.OnItemClick
import com.theone.busandbt.dto.Selection

class SelectionListAdapter<T>(selectionList: List<Selection<T>> = emptyList()) :
    DataBindingListAdapter<ItemSelectionBinding, Selection<T>>() {

    init {
        _items.addAll(selectionList)
    }

    override val viewHolderLayoutId: Int = R.layout.item_selection
    override var beforeItemClick: OnItemClick<Selection<T>>? = { view, position, item ->
        val oldView = selectedView
        if (oldView != view) {
            val old = selectedItem
            val oldPosition = selectedPosition
            if (oldView != null && old != null && oldPosition != null) {
                oldView.isSelected = false
                notifyItemChanged(oldPosition, 1)
            }
            view.isSelected = true
            selectedView = view
            selectedItem = item.data
            selectedPosition = position
            notifyItemChanged(position, 1)
        }
    }
    private var selectedView: View? = null
    private var selectedItem: T? = null
    private var selectedPosition: Int? = null

    override fun doBind(
        binding: ItemSelectionBinding,
        item: Selection<T>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            if (payloads.isEmpty()) {
                title = item.title
                root.isSelected = item.isSelected
            }
            titleTextView.setTextColor(if (root.isSelected) MAIN_COLOR else MAIN_TEXT_COLOR)
            titleTextView.typeface =
                if (root.isSelected) root.context.sultBold else root.context.sultMedium
            checkImageView.visibility = if (root.isSelected) View.VISIBLE else View.INVISIBLE
        }
    }
}