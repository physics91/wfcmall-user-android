package com.theone.busandbt.adapter.review

import com.bumptech.glide.Glide
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemReviewWriteImageBinding
import com.theone.busandbt.dto.review.ReviewFile
import com.theone.busandbt.eventbus.RemoveReviewImageEvent
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.item.SetReviewImage
import org.greenrobot.eventbus.EventBus

/**
 * 리뷰쓰기 사진 어뎁터
 */
class SetReviewImageListAdapter(itemList: List<ReviewFile>) :
    DataBindingListAdapter<ItemReviewWriteImageBinding, SetReviewImage>() {

    init {
        _items.addAll(itemList.map { SetReviewImage(reviewFile = it) })
    }

    override val viewHolderLayoutId: Int = R.layout.item_review_write_image

    override fun doBind(
        binding: ItemReviewWriteImageBinding,
        item: SetReviewImage,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            reviewImage.clipToOutline = true
            when {
                item.reviewFile != null -> CommonBindingAdapter.glideImageUrl(reviewImage, item.reviewFile.path)
                item.imageUri != null -> {
                    Glide.with(reviewImage).load(item.imageUri).into(reviewImage)
                }
            }
            removeButton.setOnClickListener {
                remove(item)
                EventBus.getDefault().post(RemoveReviewImageEvent())
            }
        }
    }
}