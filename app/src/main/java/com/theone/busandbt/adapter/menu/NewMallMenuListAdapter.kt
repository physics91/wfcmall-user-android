package com.theone.busandbt.adapter.menu

import android.graphics.Paint
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemNewMallMenuBinding
import com.theone.busandbt.dialog.MallMenuBasketAddDialog
import com.theone.busandbt.dto.menu.NewAndPopularMallMenuListItem
import com.theone.busandbt.adapter.DataBindingListAdapter

class NewMallMenuListAdapter(private val fragmentManager: FragmentManager) :
    DataBindingListAdapter<ItemNewMallMenuBinding, NewAndPopularMallMenuListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_new_mall_menu

    override fun doBind(
        binding: ItemNewMallMenuBinding,
        item: NewAndPopularMallMenuListItem,
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
                CommonBindingAdapter.commonCost(originalPriceTextView, item.cost)
            } else {
                originalPriceTextView.isVisible = false
            }
            CommonBindingAdapter.commonCost(priceTextView, item.saleCost)
            goToBasketImageView.setOnClickListener {
                with(MallMenuBasketAddDialog()) {
                    arguments = bundleOf("menuId" to item.id)
                    show(this@NewMallMenuListAdapter.fragmentManager, "")
                }
            }
        }
    }
}