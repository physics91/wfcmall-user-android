package com.theone.busandbt.adapter.shop

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.bindingadapter.ShopBindingAdapter
import com.theone.busandbt.databinding.ItemRestaurantlistBinding
import com.theone.busandbt.dto.shop.ShopListItem
import com.theone.busandbt.extension.toLocalDateTime
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.busandbt.code.DeliveryType
import java.time.LocalDateTime

/**
 * 음식점 가게리스트들을 연결해주는 어뎁터다.
 *  음식점 가게리스트 RecyclerView에 아이템 연결에 사용 중
 */
class FoodListAdapter(private val deliveryType: DeliveryType) :
    DataBindingListAdapter<ItemRestaurantlistBinding, ShopListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_restaurantlist

    override fun doBind(
        binding: ItemRestaurantlistBinding,
        item: ShopListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            shop = item
            restaurantListLogoImageView.clipToOutline = true
            CommonBindingAdapter.glideImageUrlWithReplacement(
                restaurantListLogoImageView,
                item.imageUrl,
                AppCompatResources.getDrawable(root.context, R.drawable.img_not_shop_common)
            )
            // 주문 완료 예상시간을 결정한다.
            val orderDoneMinutes = when (deliveryType) {
                DeliveryType.PACKAGING -> item.packagingDoneMinutes
                else -> item.deliveryDoneMinutes
            }
            ShopBindingAdapter.orderDoneMinutesForm(orderDoneMinutesTextView, orderDoneMinutes)

            // 신규 마크 보임/안보임을 결정한다.
            // 현재 시각이 신규상점 시간보다 이전이면 신규마크를 보인다.
            val newDisplayDateTime = item.newDisplayDateTime
            newShopLabel.visibility = if (newDisplayDateTime != null && LocalDateTime.now()
                    .isBefore(newDisplayDateTime.toLocalDateTime())
            ) View.VISIBLE else View.GONE

            //TODO : 준비중 데이터가 들어오면 처리해야함
     /*       grayView.isVisible=true
            readyTextView.isVisible=true
            restaurantListTitleTextView.setTextColor(disable)
            restaurantListRatedTextView.setTextColor(disable)
            orderDoneMinutesTextView.setTextColor(disable)
            minOrderTextView.setTextColor(disable)
            restaurantListMinPriceTextView.setTextColor(disable)
            tipTextView.setTextColor(disable)
            restaurantListTipPrice.setTextColor(disable)*/
        }
    }
}