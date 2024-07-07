package com.theone.busandbt.adapter.review

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemCreateReviewRecommendListBinding
import com.theone.busandbt.dto.review.ReviewMenu
import com.theone.busandbt.adapter.DataBindingListAdapter

/**
 * 리뷰쓰기 추천 메뉴 어뎁터
 */
class SetReviewMenuListAdapter(menuList: List<ReviewMenu>) :
    DataBindingListAdapter<ItemCreateReviewRecommendListBinding, ReviewMenu>() {

    private val recommendMap = HashMap<ReviewMenu, Boolean>()

    init {
        _items.addAll(menuList)
    }

    override val viewHolderLayoutId: Int = R.layout.item_create_review_recommend_list
    override fun doBind(
        binding: ItemCreateReviewRecommendListBinding,
        item: ReviewMenu,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menuName = item.menuName
            recommendCheckbox.isChecked = item.recommended
            recommendCheckbox.setOnCheckedChangeListener { _, isChecked ->
                recommendMap[item] = isChecked
            }
        }
    }
}