package com.theone.busandbt.adapter.search

import android.graphics.Paint
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemShoppingSearchBinding
import com.theone.busandbt.dialog.MallMenuBasketAddDialog
import com.theone.busandbt.dto.menu.MallMenuListItem
import com.theone.busandbt.adapter.DataBindingListAdapter

/**
 * 쇼핑몰 리사이클러뷰 어뎁터
 * TODO: 쇼핑몰 전용 검색 아이템이 필요함 없는 값이 많음.
 */

class SearchShoppingResultListAdapter(
    private val fragmentManager: FragmentManager
) :
    DataBindingListAdapter<ItemShoppingSearchBinding, MallMenuListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_shopping_search

    override fun doBind(
        binding: ItemShoppingSearchBinding,
        item: MallMenuListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menu = item
            originalPriceTextView.paintFlags =
                originalPriceTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            newLogoImageView.clipToOutline = true
            if (item.discountRate > 0 && item.saleCost > 0) {
                originalPriceTextView.isVisible = true
                discountRateTextView.text = "${item.discountRate}%"
                CommonBindingAdapter.commonCost(originalPriceTextView, item.cost)
            } else {
                originalPriceTextView.isVisible = false
            }
            CommonBindingAdapter.commonCost(priceTextView, item.saleCost)
            goToBasketImageView.setOnClickListener {
                with(MallMenuBasketAddDialog()) {
                    arguments = bundleOf("menuId" to item.id)
                    show(this@SearchShoppingResultListAdapter.fragmentManager, null)
                }
            }
        }
    }
}