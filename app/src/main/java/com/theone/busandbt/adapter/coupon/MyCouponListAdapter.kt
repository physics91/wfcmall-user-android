package com.theone.busandbt.adapter.coupon

import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.busandbt.code.CouponUseType
import com.busandbt.code.DeliveryType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemMyCouponBinding
import com.theone.busandbt.dto.coupon.MemberCouponListItem
import com.theone.busandbt.extension.toCouponUseDateString
import com.theone.busandbt.extension.toLocalDateTime
import com.theone.busandbt.instance.COUPON_IMAGE_RESOURCES
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class MyCouponListAdapter :
    DataBindingListAdapter<ItemMyCouponBinding, MemberCouponListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_my_coupon

    override fun doBind(
        binding: ItemMyCouponBinding,
        item: MemberCouponListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            memberCouponListItem = item
            val imageIndex = position % COUPON_IMAGE_RESOURCES.size
            Glide.with(root).load(COUPON_IMAGE_RESOURCES[imageIndex]).into(couponImageView)
            val now = LocalDateTime.now()
            val useEndDateTime = when (item.useType) {
                CouponUseType.DAYS.id -> {
                    if (item.availableDays <= 0) return
                    item.issuedDateTime.toLocalDateTime().plusDays(item.availableDays.toLong())
                }

                CouponUseType.PERIOD.id -> {
                    if (item.useEndDateTime == null) return
                    item.useEndDateTime.toLocalDateTime()
                }

                else -> return
            }
            deliveryTypeRecyclerView.addItemDecoration(
                VerticalSpaceItemDecoration(
                    SizeUtils.dp2px(3F),
                    doLast = false,
                    doFirst = false
                )
            )
            if (item.deliveryTypeList.isNotEmpty()) {
                deliveryTypeRecyclerView.adapter =
                    CouponDeliveryTypeListAdapter(item.deliveryTypeList.map { DeliveryType.find(it) })
                deliveryTypeRecyclerView.isVisible = true
            } else {
                deliveryTypeRecyclerView.isVisible = false
            }
            val d = Duration.between(useEndDateTime, now)
            couponDeadlineTextView.text = "${d.toDays().absoluteValue}일 남음"
            couponDateTextView.text =
                "(${useEndDateTime?.toCouponUseDateString()}까지)"
        }
    }
}