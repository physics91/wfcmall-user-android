package com.theone.busandbt.adapter.coupon

import androidx.core.view.isVisible
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemCouponOutsideBinding
import com.theone.busandbt.dto.coupon.MemberCouponByShop
import com.theone.busandbt.adapter.DataBindingListAdapter


/**
 * TODO: shop 아이디와 쿠폰리스트를 들고 있는 dto 가 필요함 지금 아이템은 변경 해줘야함
 */
class CouponListAdapter :
    DataBindingListAdapter<ItemCouponOutsideBinding, MemberCouponByShop>() {
    override val viewHolderLayoutId: Int = R.layout.item_coupon_outside
    private val couponAdapterList = HashMap<MemberCouponByShop, CouponItemListAdapter>()

    override fun doBind(
        binding: ItemCouponOutsideBinding,
        item: MemberCouponByShop,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            memberCoupon = item
            couponRecyclerView.adapter =
                couponAdapterList.getOrPut(item) { CouponItemListAdapter(item.toItemList()) }
            downArrowImageView.setOnCheckedChangeListener { _, isChecked ->
                couponRecyclerView.isVisible = !isChecked
            }
        }
    }
}