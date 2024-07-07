package com.theone.busandbt.adapter.basket

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.bindingadapter.ShopBindingAdapter
import com.theone.busandbt.databinding.ItemShoppingBasketListBinding
import com.theone.busandbt.dto.shop.BasketInfo
import com.theone.busandbt.eventbus.basket.ChangeSelectedBasketShopEvent
import com.theone.busandbt.extension.calculateMenuCost
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.findDeliveryCost
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.basket.BasketMainFragmentDirections
import com.theone.busandbt.fragment.shop.ShoppingDetailFragmentArgs
import com.theone.busandbt.item.basket.BasketShop
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.greenrobot.eventbus.EventBus

/**
 * 장바구니 아이템 중첩리사이클 바깥쪽 아이템
 */
class BasketShopListAdapter(
    private val deliveryType: DeliveryType,
    private val basketInfoMap: Map<Int, BasketInfo>
) : DataBindingListAdapter<ItemShoppingBasketListBinding, BasketShop>() {
    override val viewHolderLayoutId: Int = R.layout.item_shopping_basket_list
    private val menuAdapterList = HashMap<BasketShop, BasketMenuListAdapter>()
    private var _selectedItem: BasketShop? = null
    val selectedItem: BasketShop? get() = _selectedItem
    private var selectedPosition: Int? = null

    override fun doBind(
        binding: ItemShoppingBasketListBinding,
        item: BasketShop,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            shop = item
            foodAllMenuRadioButton.setOnClickListener {
                if (selectedItem == item) return@setOnClickListener
                if (item.isSelected) return@setOnClickListener
                val old = selectedItem
                val oldPosition = selectedPosition
                if (old != null && oldPosition != null && old != item) {
                    old.isSelected = false
                    old.menuList.forEach { menu -> menu.isSelected = false }
                    notifyItemChanged(oldPosition)
                }
                _selectedItem = item
                selectedPosition = position
                item.isSelected = true
                item.menuList.forEach { menu -> menu.isSelected = true }
                notifyItemChanged(position)
                EventBus.getDefault().post(ChangeSelectedBasketShopEvent(old, item))
            }

            debugLog("장바구니 메뉴", item.menuList)
            itemListRecyclerView.adapter =
                menuAdapterList.getOrPut(item) {
                    BasketMenuListAdapter(
                        item,
                        this@BasketShopListAdapter
                    )
                }
            if (itemListRecyclerView.itemDecorationCount == 0) itemListRecyclerView.addItemDecoration(
                HorizontalSpaceItemDecoration(SizeUtils.dp2px(10F))
            )

            goShopDetailButton.setOnClickListener {
                when (item.serviceTypeId) {
                    ServiceType.FOOD_DELIVERY.id -> {
                        val action =
                            BasketMainFragmentDirections.actionShopBasketMainFragmentToShopDetailsFragment(
                                item.shopId,
                                item.serviceTypeId,
                                deliveryType.id
                            )
                        it.findNavController().navigate(action)
                    }

                    ServiceType.SHOPPING_MALL.id -> {
                        it.navigate(
                            R.id.shoppingdetails_graph,
                            ShoppingDetailFragmentArgs(
                                item.shopId,
                                item.serviceTypeId,
                                item.deliveryTypeId
                            ).toBundle()
                        )
                    }
                }
            }

            //배달비가 없을 때 가운대 정렬
            deliveryCostTextView.isVisible = deliveryType != DeliveryType.PACKAGING
            if (!deliveryCostTextView.isVisible) {
                val set = ConstraintSet()
                set.clone(constraintLayout)
                set.connect(
                    R.id.goShopDetailButton,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START,
                    0
                )
                set.connect(
                    R.id.goShopDetailButton,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END,
                    0
                )
                set.applyTo(constraintLayout)
            }
            if (deliveryType != DeliveryType.PACKAGING) {
                val basketInfo = basketInfoMap[item.shopId] ?: return
                val menuCost = item.menuList.calculateMenuCost()
                val deliveryCost = basketInfo.deliveryCostList.findDeliveryCost(menuCost) + basketInfo.extraCost
                when (deliveryType) {
                    DeliveryType.PARCEL -> {
                        ShopBindingAdapter.parcelCostForm(deliveryCostTextView, deliveryCost)
                    }
                    else -> {
                        ShopBindingAdapter.deliveryCostForm(deliveryCostTextView, deliveryCost)
                    }
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return _items[position].id
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        menuAdapterList.clear()
    }

    override fun setItems(items: Collection<BasketShop>) {
        super.setItems(items)
        _selectedItem = items.find { it.isSelected }
        selectedPosition = items.indexOfFirst { it.isSelected }
    }

    fun refresh(item: BasketShop) {
        val index = items.indexOf(item)
        if (index >= 0) notifyItemChanged(index)
    }
}