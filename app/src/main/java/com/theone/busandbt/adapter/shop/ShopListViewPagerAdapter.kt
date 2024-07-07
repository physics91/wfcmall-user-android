package com.theone.busandbt.adapter.shop

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.fragment.shop.FoodListItemFragment
import com.theone.busandbt.fragment.shop.FoodListItemFragmentArgs
import kotlin.math.min

/**
 * 탭 클릭시 변경되는 프래그먼트들을 연결하는 어뎁터
 * 한식,치킨,중식,분식 등등 ...
 */
class ShopListViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val categoryList = mutableListOf<CategoryListItem>()
    override fun getItemCount(): Int = categoryList.size

    override fun createFragment(position: Int): Fragment = FoodListItemFragment().apply {
        arguments = FoodListItemFragmentArgs(categoryList[position].id).toBundle()
    }

    fun getCategory(position: Int) = categoryList[position]

    fun setItems(items: List<CategoryListItem>) {
        val oldSize = categoryList.size
        val newSize = items.size
        categoryList.clear()
        categoryList.addAll(items)
        notifyItemRangeChanged(0, min(newSize, oldSize))
        if (oldSize > newSize) notifyItemRangeRemoved(newSize, oldSize - newSize)
        if (newSize > oldSize) notifyItemRangeInserted(oldSize, newSize - oldSize)
    }
}