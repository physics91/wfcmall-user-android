package com.theone.busandbt.adapter.menu

import android.text.SpannedString
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.buildSpannedString
import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemMenuBinding
import com.theone.busandbt.dto.menu.Menu
import com.theone.busandbt.extension.sultRegular
import com.theone.busandbt.extension.sultSemiBold
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.instance.EMPTY_MENU_IMAGE_RESOURCES
import com.theone.busandbt.spanned.TypefaceSpanCompat

/**
 * 맨 아래 세트메뉴 안쪽 아이템 RecyclerView 어뎁터
 */
class MenuAdapter(
    menuList: List<Menu>,
    private val emptyImageResourceMap: Map<Int, Int>
) : DataBindingListAdapter<ItemMenuBinding, Menu>() {
    override val viewHolderLayoutId: Int = R.layout.item_menu

    init {
        _items.addAll(menuList)
    }

    override fun doBind(
        binding: ItemMenuBinding,
        item: Menu,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menu = item
            if (item.imageUrl.isNotEmpty()) {
                CommonBindingAdapter.glideImageUrl(menuImageView, item.imageUrl)
            } else {
                val emptyImageResourceIndex = emptyImageResourceMap[position]
                if (emptyImageResourceIndex != null) Glide.with(root).load(EMPTY_MENU_IMAGE_RESOURCES[emptyImageResourceIndex]).into(menuImageView)
            }
            menuNameTextView.setCompoundDrawablesWithIntrinsicBounds(
                null, null, if (item.isAdultMenu) AppCompatResources.getDrawable(
                    root.context,
                    R.drawable.ic_adult_main
                ) else null, null
            )
            menuImageView.clipToOutline = true
            menuCostTextView.text = buildSpannedString {
                val isAllUnNamed = item.menuCostList.find { it.name.isNotEmpty() } == null
                if (isAllUnNamed) {
                    append((item.menuCostList.firstOrNull()?.saleCost ?: 0).toMoneyFormat())
                } else {
                    item.menuCostList.forEachIndexed { index, menuCostListItem ->
                        if (menuCostListItem.name.isEmpty()) return@forEachIndexed
                        append(
                            "${menuCostListItem.name} : ", TypefaceSpanCompat(root.context.sultRegular),
                            SpannedString.SPAN_EXCLUSIVE_INCLUSIVE
                        )
                        append(
                            menuCostListItem.saleCost.toMoneyFormat(), TypefaceSpanCompat(root.context.sultSemiBold),
                            SpannedString.SPAN_EXCLUSIVE_INCLUSIVE
                        )
                        if (index != item.menuCostList.size - 1) append("\n")
                    }
                }
            }
        }
    }
}