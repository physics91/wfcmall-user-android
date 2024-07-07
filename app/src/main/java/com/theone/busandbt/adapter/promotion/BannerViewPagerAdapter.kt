package com.theone.busandbt.adapter.promotion

import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemLayoutBannerBinding
import com.theone.busandbt.dto.promotion.PromotionListItem

/**
 * 배너 어뎁터입니다
 */
class BannerViewPagerAdapter :
    DataBindingListAdapter<ItemLayoutBannerBinding, PromotionListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_layout_banner

    override fun doBind(
        binding: ItemLayoutBannerBinding,
        item: PromotionListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            Glide.with(root.context).load(item.imageUrl).thumbnail(0.1f).into(bannerImage)
        }
    }
}