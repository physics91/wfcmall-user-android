package com.theone.busandbt.adapter.shop

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.theone.busandbt.adapter.StoredPagerAdapter
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.fragment.menu.ShoppingSmallCategoryFragment

/**
 * 전통시장 바로배달 먹거리 -> (전체, 종류) 어뎁터
 */
class MallMiddleCategoryPagerAdapter(
    override val fragment: Fragment,
    private val categoryList: List<CategoryListItem>,
    private val shopId: Int,
    private val viewType: Int
) : StoredPagerAdapter(fragment) {

    override fun getItemCount(): Int = categoryList.size

    override fun defineFragment(position: Int): Fragment {
        val item = categoryList[position]
        return ShoppingSmallCategoryFragment().apply {
            arguments = bundleOf(
                "category" to item,
                "categoryList" to item.lowerCategoryList.toTypedArray(),
                "shopId" to shopId,
                "viewType" to viewType,
                "position" to position
            )
        }
    }
}