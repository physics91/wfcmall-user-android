package com.theone.busandbt.adapter.promotion

import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.databinding.ItemPromotionListBinding
import com.theone.busandbt.dto.promotion.PromotionListItem

/**
 * 프로모션 배너 모두보기 어뎁터
 * TODO - 배너 아이템 클릭 시 랜딩 페이지로 이동해야함
 */
class PromotionListAdapter : DataBindingListAdapter<ItemPromotionListBinding, PromotionListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_promotion_list

    override fun doBind(
        binding: ItemPromotionListBinding,
        item: PromotionListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            promotion = item
            bannerImageView.clipToOutline = true
        }
    }
}