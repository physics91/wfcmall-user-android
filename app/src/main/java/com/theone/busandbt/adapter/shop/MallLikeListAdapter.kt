package com.theone.busandbt.adapter.shop

import androidx.core.view.isVisible
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemLikeListShoppingBinding
import com.theone.busandbt.dto.shop.MallLikeListItem
import com.theone.busandbt.adapter.DataBindingListAdapter

/**
 * 쇼핑몰 찜화면 어댑터
 */
class MallLikeListAdapter :
    DataBindingListAdapter<ItemLikeListShoppingBinding, MallLikeListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_like_list_shopping
    private var allEditCheck: Boolean? = null
    private val selectedItems = ArrayList<MallLikeListItem>()

    override fun doBind(
        binding: ItemLikeListShoppingBinding,
        item: MallLikeListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            shop = item
            val aec = allEditCheck
            restaurantListLogoImageView.clipToOutline = true
            if (aec != null) {
                editCheckBox.isChecked = aec
                editCheckBox.isVisible = true
            } else {
                editCheckBox.isVisible = false
            }
            editCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedItems.add(item) else selectedItems.remove(item)
            }

        }
    }

    fun toggleEditMode(flag: Boolean) {
        allEditCheck = if (flag) false else null
        notifyItemRangeChanged(0, _items.size)
    }

    fun toggleAllEditCheckBox(flag: Boolean) {
        allEditCheck = flag
        notifyItemRangeChanged(0, _items.size)
    }

    fun getSelectedItems(): List<MallLikeListItem> = selectedItems

    fun removeSelectedItems() {
        removeList(selectedItems)
        selectedItems.clear()
    }
}