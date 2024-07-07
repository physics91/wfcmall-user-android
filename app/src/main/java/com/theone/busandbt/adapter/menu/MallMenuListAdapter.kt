package com.theone.busandbt.adapter.menu

import android.graphics.Paint
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.busandbt.code.MenuStatus
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemMallMenuBinding
import com.theone.busandbt.dialog.MallMenuBasketAddDialog
import com.theone.busandbt.dto.menu.MallMenuListItem
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.extension.showMessageBar

/**
 * 쇼핑몰 메뉴 아이템 리스트 어뎁터
 */
class MallMenuListAdapter(private val fragmentManager: FragmentManager) :
    DataBindingListAdapter<ItemMallMenuBinding, MallMenuListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_mall_menu

    override fun doBind(
        binding: ItemMallMenuBinding,
        item: MallMenuListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menu = item
            newLogoImageView.clipToOutline = true
            originalPriceTextView.paintFlags =
                originalPriceTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            if (item.discountRate > 0 && item.saleCost > 0) {
                originalPriceTextView.isVisible = true
                discountRateTextView.text = "${item.discountRate}%"
                CommonBindingAdapter.commonCost(originalPriceTextView, item.cost)
            } else {
                originalPriceTextView.isVisible = false
            }
            CommonBindingAdapter.commonCost(priceTextView, item.saleCost)
            goToBasketImageView.setOnClickListener {
                if (item.status != MenuStatus.SELLING.id) {
                    root.showMessageBar("현재 구매할 수 없는 상품입니다.")
                    return@setOnClickListener
                }
                with(MallMenuBasketAddDialog()) {
                    arguments = bundleOf("menuId" to item.id)
                    show(this@MallMenuListAdapter.fragmentManager, null)
                }
            }
        }
    }
}