package com.theone.busandbt.adapter.basket

import androidx.appcompat.content.res.AppCompatResources
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemBasketInBinding
import com.theone.busandbt.eventbus.basket.ChangeBasketMenuEvent
import com.theone.busandbt.eventbus.basket.RemoveBasketMenuEvent
import com.theone.busandbt.extension.calculateOptionCost
import com.theone.busandbt.extension.signText
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.item.basket.BasketMenu
import com.theone.busandbt.item.basket.BasketShop
import org.greenrobot.eventbus.EventBus

/**
 * 장바구니 아이템 중첩리사이클 안쪽 아이템
 */
class BasketMenuListAdapter(
    private val basketShop: BasketShop,
    private val parentAdapter: BasketShopListAdapter,
) :
    DataBindingListAdapter<ItemBasketInBinding, BasketMenu>() {
    override val viewHolderLayoutId: Int = R.layout.item_basket_in

    init {
        this._items.addAll(basketShop.menuList)
    }

    override fun doBind(
        binding: ItemBasketInBinding,
        item: BasketMenu,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menu = item
            menuImageView.clipToOutline = true
            if (item.imageUrl != menuImageView.tag) {
                CommonBindingAdapter.glideImageUrlWithReplacement(
                    menuImageView,
                    item.imageUrl,
                    AppCompatResources.getDrawable(root.context, R.drawable.ic_logo_thumbnail)
                )
                menuImageView.tag = item.imageUrl
            }
            countTextView.text = "${item.count}"
            menuCostTextView.text = root.context.getString(
                R.string.commonCost,
                (item.count * (item.saleCost + item.optionList.calculateOptionCost())).toMoneyFormat()
            )
            optionTextView.text = buildString {
                append("ㆍ가격: ")
                val menuCostMoneyForm = item.saleCost.toCommonMoneyForm()
                if (item.menuCostName.isNotEmpty()) append("${item.menuCostName}($menuCostMoneyForm)") else append(
                    menuCostMoneyForm
                )
                item.optionList.groupBy { it.optionGroupName }.forEach { (groupName, optionList) ->
                    append("\nㆍ${groupName}: ")
                    optionList.forEachIndexed { index, option ->
                        append(option.name)
                        if (option.cost != 0) append("(${option.cost.signText()}${option.cost.toMoneyFormat()}원)")
                        if (index != optionList.size - 1) append("/")
                    }
                }
            }
            removeButton.setOnClickListener {
                EventBus.getDefault().post(RemoveBasketMenuEvent(basketShop, item))
                remove(item)
            }
            plusImageView.setOnClickListener {
                val count = countTextView.text.toString().toInt()
                if (count >= 999) return@setOnClickListener
                item.count += 1
                onChangedMenuCount(item, position)
            }
            minusImageView.setOnClickListener {
                val count = countTextView.text.toString().toInt()
                if (count <= 1) return@setOnClickListener
                item.count -= 1
                onChangedMenuCount(item, position)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return _items[position].id
    }

    private fun onChangedMenuCount(item: BasketMenu, position: Int) {
        if (basketShop.isSelected) EventBus.getDefault()
            .post(ChangeBasketMenuEvent(basketShop, item))
        notifyItemChanged(position)
        parentAdapter.refresh(basketShop)
    }
}