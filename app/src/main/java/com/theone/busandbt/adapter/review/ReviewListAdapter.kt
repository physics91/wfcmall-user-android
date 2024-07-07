package com.theone.busandbt.adapter.review

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.theone.busandbt.R
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.ItemFullReviewListBinding
import com.theone.busandbt.dto.review.ReviewListItem
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.google.android.material.chip.Chip

/**
 * 전체리뷰 리사이클러뷰 어뎁터
 */
class ReviewListAdapter :
    DataBindingListAdapter<ItemFullReviewListBinding, ReviewListItem>() {
    override val viewHolderLayoutId: Int = R.layout.item_full_review_list
    var isVisibleImage = true

    override fun doBind(
        binding: ItemFullReviewListBinding,
        item: ReviewListItem,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            review = item
            foodImgViewPager.clipToOutline = true
            baseRatingBar.rating = item.star.toFloat()
            presidentView.visibility = if (item.shopReply != null) View.VISIBLE else View.GONE
            CommonBindingAdapter.glideImageUrlWithReplacement(
                writerProfileImageView,
                item.writerProfileImageUrl,
                AppCompatResources.getDrawable(root.context, R.drawable.ic_myinfo_profile_img)
            )
            reviewMenuChipGroup.removeAllViews()
            item.menuList.forEach {
                val chip = LayoutInflater.from(root.context).inflate(R.layout.layout_review_menu_chip, reviewMenuChipGroup, false) as Chip
                chip.isChecked = it.recommended
                chip.text = it.menuName
                reviewMenuChipGroup.addView(chip)
            }

            if (isVisibleImage) {
                foodImgViewPager.isVisible = item.imageFileList.isNotEmpty()
                countTextView.isVisible = item.imageFileList.isNotEmpty()
                foodImgViewPager.adapter = ReviewImageListAdapter(item.imageFileList)
                foodImgViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        countTextView.text = (position + 1).toString() +"/"+ item.imageFileList.size
                    }
                })
            } else {
                foodImgViewPager.isVisible = false
                countTextView.isVisible = false
            }
        }
    }
}