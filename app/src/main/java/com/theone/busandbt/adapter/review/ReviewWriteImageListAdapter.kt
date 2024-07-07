package com.theone.busandbt.adapter.review

import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemReviewWriteImageBinding
import com.theone.busandbt.eventbus.RemoveReviewImageEvent
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.item.ReviewWriteImage
import org.greenrobot.eventbus.EventBus

/**
 * 리뷰쓰기 사진 어뎁터
 */
class ReviewWriteImageListAdapter :
    DataBindingListAdapter<ItemReviewWriteImageBinding, ReviewWriteImage>() {
    override val viewHolderLayoutId: Int = R.layout.item_review_write_image

    override fun doBind(
        binding: ItemReviewWriteImageBinding,
        item: ReviewWriteImage,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            photoList = item
            reviewImage.clipToOutline = true
            Glide.with(reviewImage).load(item.imageUri).into(reviewImage)
            removeButton.setOnClickListener {
                remove(item)
                EventBus.getDefault().post(RemoveReviewImageEvent())
            }
        }
    }
}