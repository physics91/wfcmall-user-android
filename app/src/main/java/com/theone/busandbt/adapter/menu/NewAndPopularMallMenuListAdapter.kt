package com.theone.busandbt.adapter.menu

import android.graphics.Paint
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemNewAndPopularMallMenuBinding
import com.theone.busandbt.dialog.MallMenuBasketAddDialog
import com.theone.busandbt.dto.menu.NewAndPopularMallMenuListItem


/**
 * 택배배송 메인화면에 들어가는 인기상품, 신규상품 리스트 어댑터
 * 전체보기에서 쓰는게 아니다.
 */
class NewAndPopularMallMenuListAdapter(private val fragmentManager: FragmentManager) :
    DataBindingListAdapter<ItemNewAndPopularMallMenuBinding, NewAndPopularMallMenuListItem>() {

    override val viewHolderLayoutId: Int = R.layout.item_new_and_popular_mall_menu

    override fun doBind(
        binding: ItemNewAndPopularMallMenuBinding,
        item: NewAndPopularMallMenuListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            shoppingItem = item
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
            basketButton.setOnClickListener {
                with(MallMenuBasketAddDialog()) {
                    arguments = bundleOf("menuId" to item.id)
                    show(this@NewAndPopularMallMenuListAdapter.fragmentManager, null)
                }
            }
        }
    }
}