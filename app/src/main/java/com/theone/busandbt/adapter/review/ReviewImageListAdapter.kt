package com.theone.busandbt.adapter.review

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemReviewImageListBinding
import com.theone.busandbt.dto.review.ReviewFile
import com.theone.busandbt.adapter.DataBindingListAdapter

/**
 * 전체리뷰보기 리사이클러뷰 아이템 안에있는 뷰페이저2 의 어뎁터
 */
class ReviewImageListAdapter(imageUrlList: List<ReviewFile> = emptyList()) :
    DataBindingListAdapter<ItemReviewImageListBinding, ReviewFile>() {

    override val viewHolderLayoutId: Int = R.layout.item_review_image_list

    init {
        _items.addAll(imageUrlList)
    }

    override fun doBind(
        binding: ItemReviewImageListBinding,
        item: ReviewFile,
        position: Int,
        payloads: MutableList<Any>
    ) {
        binding.imageUrl = item.path
    }
}