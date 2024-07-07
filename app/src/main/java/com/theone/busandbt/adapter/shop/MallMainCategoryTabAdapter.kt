package com.theone.busandbt.adapter.shop

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.theone.busandbt.adapter.StoredPagerAdapter
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.fragment.menu.ShoppingListItemFragment

/**
 * 전통시장 바로배달 (전체 , 먹거리) 탭 어뎁터
 * */
class MallMainCategoryTabAdapter(
    override val fragment: Fragment,
    private val categoryList: List<CategoryListItem>,
    private val shopId: Int = 0,
    private val viewType: Int = 0
) : StoredPagerAdapter(fragment) {

    override fun getItemCount(): Int = categoryList.size

    override fun defineFragment(position: Int): Fragment {
        val item = categoryList[position]
        return ShoppingListItemFragment().apply {
            arguments = bundleOf(
                "category" to item,
                "shopId" to shopId,
                "viewType" to viewType,
                "position" to position
            )
        }
    }
}