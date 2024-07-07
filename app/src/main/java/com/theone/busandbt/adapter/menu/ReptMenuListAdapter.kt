package com.theone.busandbt.adapter.menu

import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemReptMenuBinding
import com.theone.busandbt.dto.menu.Menu
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.instance.EMPTY_MENU_IMAGE_RESOURCES

/**
 * 메뉴 RecyclerView어뎁터
 */
class ReptMenuListAdapter(reptMenuList: List<Menu> = emptyList()) :
    DataBindingListAdapter<ItemReptMenuBinding, Menu>() {
    override val viewHolderLayoutId: Int = R.layout.item_rept_menu
    private val emptyMap: Map<Int, Int> by lazy {
        var imagePosition = 0
        val innerResult = HashMap<Int, Int>()
        reptMenuList.forEachIndexed { i, menu ->
            if (menu.imageUrl.isNotEmpty()) return@forEachIndexed
            innerResult[i] = imagePosition
            if (imagePosition >= EMPTY_MENU_IMAGE_RESOURCES.size - 1) imagePosition = 0 else imagePosition++
        }
        innerResult
    }

    init {
        _items.addAll(reptMenuList)
    }

    override fun doBind(
        binding: ItemReptMenuBinding,
        item: Menu,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menu = item
            if (item.imageUrl.isNotEmpty()) {
                CommonBindingAdapter.glideImageUrl(foodImg, item.imageUrl)
            } else {
                val index = emptyMap[position]
                if (index != null) Glide.with(root).load(EMPTY_MENU_IMAGE_RESOURCES[index]).into(foodImg)
            }
            foodName.setCompoundDrawablesWithIntrinsicBounds(
                null, null, if (item.isAdultMenu) AppCompatResources.getDrawable(
                    root.context,
                    R.drawable.ic_adult_main
                ) else null, null
            )
            foodImg.clipToOutline = true
            val minMenuCost = item.menuCostList.minOfOrNull { it.saleCost } ?: 0
            val maxMenuCost = item.menuCostList.maxOfOrNull { it.saleCost } ?: 0
            menuCostTextView.text =
                if (minMenuCost != maxMenuCost) "${minMenuCost.toMoneyFormat()} ~ ${maxMenuCost.toMoneyFormat()}" else minMenuCost.toMoneyFormat()
        }
    }
}