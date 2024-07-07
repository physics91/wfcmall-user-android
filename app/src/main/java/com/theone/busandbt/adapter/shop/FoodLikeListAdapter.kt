package com.theone.busandbt.adapter.shop

import android.view.View
import androidx.core.view.isVisible
import com.busandbt.code.DeliveryType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemMyWishListDeliveryBinding
import com.theone.busandbt.dto.shop.LikedFoodListItem
import com.theone.busandbt.extension.toLocalDateTime
import java.time.LocalDateTime

class FoodLikeListAdapter(private val deliveryTypeId: Int) :
    DataBindingListAdapter<ItemMyWishListDeliveryBinding, LikedFoodListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_my_wish_list_delivery
    private var allEditCheck: Boolean? = null
    private val selectedItems = ArrayList<LikedFoodListItem>()

    override fun doBind(
        binding: ItemMyWishListDeliveryBinding,
        item: LikedFoodListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            hasCouponLabel.isVisible = false
            deliveryTypeLabelTextView.isVisible = false
            shop = item
            val aec = allEditCheck
            restaurantListLogoImageView.clipToOutline = true
            if (aec != null) {
                editCheckBox.isChecked = aec
                editCheckBox.isVisible = true
            } else {
                editCheckBox.isVisible = false
            }
            val orderDoneMinutes = when (deliveryTypeId) {
                DeliveryType.INSTANT.id -> item.deliveryDoneMinutes
                DeliveryType.PACKAGING.id -> item.packagingDoneMinutes
                else -> item.deliveryDoneMinutes
            }
            editCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedItems.add(item) else selectedItems.remove(item)
            }
            orderDoneMinutesTextView.text = root.context.getString(
                R.string.orderDoneMinutesFormat,
                (orderDoneMinutes - 5).takeIf { it >= 0 } ?: 0,
                orderDoneMinutes + 5
            )
            newShopLabel.visibility = if (item.newDisplayDateTime != null && LocalDateTime.now()
                    .isBefore(item.newDisplayDateTime.toLocalDateTime())
            ) View.VISIBLE else View.GONE
        }
    }

    fun toggleEditMode(flag: Boolean) {
        allEditCheck = if (flag) false else null
        notifyItemRangeChanged(0, _items.size)
    }

    fun toggleAllEditCheckBox(flag: Boolean) {
        allEditCheck = flag
        notifyItemRangeChanged(0, _items.size)
    }

    fun getSelectedItems(): List<LikedFoodListItem> = selectedItems

    fun removeSelectedItems() {
        removeList(selectedItems)
        selectedItems.clear()
    }
}