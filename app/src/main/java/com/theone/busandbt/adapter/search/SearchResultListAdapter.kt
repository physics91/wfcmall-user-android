package com.theone.busandbt.adapter.search

import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import com.busandbt.code.DeliveryType
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemSearchResultBinding
import com.theone.busandbt.dto.shop.ShopListItem
import com.theone.busandbt.extension.sultBold
import com.theone.busandbt.extension.toLocalDateTime
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.spanned.TypefaceSpanCompat
import java.time.LocalDateTime

/**
 * 음식점 리사이클러뷰 어뎁터
 * */
class SearchResultListAdapter(private val deliveryType: DeliveryType) :
    DataBindingListAdapter<ItemSearchResultBinding, ShopListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_search_result
    var keyword: String? = null

    override fun doBind(
        binding: ItemSearchResultBinding,
        item: ShopListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            // TODO 마지막 순서일때만 아래 로직이 실행되도록
            shopListItem = item
            shopImageView.clipToOutline = true
            if (item.imageUrl.isNotEmpty()) CommonBindingAdapter.glideImageUrl(
                shopImageView,
                item.imageUrl
            ) else shopImageView.setImageResource(R.drawable.img_not_shop_common)
            val menuList = item.searchedMenuNameList
            val k = keyword
            if (!menuList.isNullOrEmpty() && k != null) {
                searchMenuNameTextView.isVisible = true
                searchMenuNameTextView.text = buildSpannedString {
                    val str = menuList.joinToString { it }
                    var startIndex = 0
                    var index = str.indexOf(k, startIndex)
                    while (index != -1) {
                        append(str.substring(startIndex until index))
                        append(
                            str.substring(index until index + k.length),
                            ForegroundColorSpan(MAIN_COLOR),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            TypefaceSpanCompat(root.context.sultBold), index, index + k.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        startIndex = index + k.length
                        index = str.indexOf(k, startIndex)
                    }
                }
            } else {
                searchMenuNameTextView.isVisible = false
            }

            val orderDoneMinutes = when (deliveryType.id) {
                DeliveryType.INSTANT.id -> item.deliveryDoneMinutes
                DeliveryType.PACKAGING.id -> item.packagingDoneMinutes
                else -> item.deliveryDoneMinutes
            }
            orderDoneMinutesTextView.text = root.context.getString(
                R.string.orderDoneMinutesFormat,
                (orderDoneMinutes - 5).takeIf { it >= 0 } ?: 0,
                orderDoneMinutes + 5
            )
            menuNew.visibility = if (item.newDisplayDateTime != null && LocalDateTime.now()
                    .isBefore(item.newDisplayDateTime.toLocalDateTime())
            ) View.VISIBLE else View.GONE
        }
    }
}