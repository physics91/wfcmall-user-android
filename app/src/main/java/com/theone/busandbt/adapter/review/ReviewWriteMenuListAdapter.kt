package com.theone.busandbt.adapter.review

import com.theone.busandbt.R
import com.theone.busandbt.databinding.ItemCreateReviewRecommendListBinding
import com.theone.busandbt.dto.order.OrderMenuInDetail
import com.theone.busandbt.adapter.DataBindingListAdapter

/**
 * 리뷰쓰기 추천 메뉴 어뎁터
 */
class ReviewWriteMenuListAdapter(menuList: List<OrderMenuInDetail> = emptyList()) :
    DataBindingListAdapter<ItemCreateReviewRecommendListBinding, OrderMenuInDetail>() {

    init {
        _items.addAll(menuList)
    }

    override val viewHolderLayoutId: Int = R.layout.item_create_review_recommend_list
    private val recommendMap = HashMap<OrderMenuInDetail, Boolean>()

    override fun doBind(
        binding: ItemCreateReviewRecommendListBinding,
        item: OrderMenuInDetail,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            menuName = item.menuName
            recommendCheckbox.setOnCheckedChangeListener { _, isChecked ->
                recommendMap[item] = isChecked
            }
        }
    }

    fun getRecommend(item: OrderMenuInDetail) = recommendMap[item]
}