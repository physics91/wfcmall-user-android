package com.theone.busandbt.adapter.coupon

import androidx.core.view.isVisible
import com.busandbt.code.CouponType
import com.busandbt.code.CouponUseType
import com.busandbt.code.DeliveryType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemMyCouponBinding
import com.theone.busandbt.dto.coupon.CouponEvent
import com.theone.busandbt.dto.coupon.MemberCouponListItem
import com.theone.busandbt.extension.toCouponUseDateString
import com.theone.busandbt.extension.toLocalDateTime
import org.greenrobot.eventbus.EventBus
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class CouponItemListAdapter(couponItemList: List<MemberCouponListItem>) :
    DataBindingListAdapter<ItemMyCouponBinding, MemberCouponListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_my_coupon
    private var selectedItem: MemberCouponListItem? = null
    private var selectedPosition: Int? = null

    init {
        this._items.addAll(couponItemList)
    }

    override fun doBind(
        binding: ItemMyCouponBinding,
        item: MemberCouponListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            memberCouponListItem = item
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
            val d = Duration.between(useEndDateTime, now)
            couponDeadlineTextView.text = "${d.toDays().absoluteValue}일 남음"
            couponDateTextView.text =
                "(${useEndDateTime.toCouponUseDateString()}까지)"

            if (item.type == CouponType.SHOP.id) {
                couponConstraintLayout.isSelected = (selectedPosition == position)
            }

            if (item.type == CouponType.EVENT.id) {
                couponConstraintLayout.isSelected = item.isSelected
            }

            if (item.deliveryTypeList.isNotEmpty()) {
                deliveryTypeRecyclerView.adapter =
                    CouponDeliveryTypeListAdapter(item.deliveryTypeList.map { DeliveryType.find(it) })
                deliveryTypeRecyclerView.isVisible = true
            } else {
                deliveryTypeRecyclerView.isVisible = false
            }

            couponConstraintLayout.setOnClickListener {
                when (item.type) {
                    CouponType.SHOP.id -> {
                        val oldItem = selectedItem
                        val oldPosition = selectedPosition
                        if (oldPosition == position) {
                            selectedPosition = null
                            selectedItem = null
                            notifyItemChanged(oldPosition)
                            EventBus.getDefault()
                                .post(CouponEvent(item, false))
                            return@setOnClickListener
                        }
                        if (oldPosition != null) notifyItemChanged(oldPosition)
                        selectedItem = item
                        selectedPosition = position
                        notifyItemChanged(position)
                        EventBus.getDefault()
                            .post(CouponEvent(item, true))
                        if (oldItem != null) {
                            EventBus.getDefault()
                                .post(CouponEvent(oldItem, false))
                        }
                    }
                    CouponType.EVENT.id -> {
                        val prevSelectedIds =
                            _items.filter { it.isSelected }.map { it.id }.toSet()

                        if (item.eventOverlapUseEnabled) {
                            // 중복 사용 가능한 쿠폰 선택 시, 중복 사용 불가능한 쿠폰들만 해제
                            _items.forEach {
                                if (!it.eventOverlapUseEnabled) it.isSelected = false
                            }
                            item.isSelected = !item.isSelected
                        } else {
                            // 중복 사용 불가능한 쿠폰 선택 시, 모든 쿠폰의 선택을 해제하고 현재 쿠폰만 선택
                            _items.forEach { it.isSelected = false }
                            item.isSelected = true
                        }

                        notifyDataSetChanged()

                        val currentSelectedIds =
                            _items.filter { it.isSelected }.map { it.id }.toSet()
                        val newSelection = currentSelectedIds - prevSelectedIds
                        val deselection = prevSelectedIds - currentSelectedIds

                        newSelection.forEach { id ->
                            EventBus.getDefault()
                                .post(_items.find { it.id == id }
                                    ?.let { it1 -> CouponEvent(it1, true) })
                        }

                        deselection.forEach { id ->
                            EventBus.getDefault()
                                .post(_items.find { it.id == id }
                                    ?.let { it1 -> CouponEvent(it1, false) })
                        }
                    }
                }
            }
        }
    }
}