package com.theone.busandbt.adapter

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemCategorySelectionBinding
import com.theone.busandbt.dto.category.CategoryListItem

/**
 * 방문포장 카테고리 선택 어댑터
 */
class CategorySelectionListAdapter :
    DataBindingListAdapter<ItemCategorySelectionBinding, CategoryListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_category_selection

    override fun doBind(
        binding: ItemCategorySelectionBinding,
        item: CategoryListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            category = item
        }
    }
}