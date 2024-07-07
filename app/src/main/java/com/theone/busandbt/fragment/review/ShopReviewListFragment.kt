package com.theone.busandbt.fragment.review

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.ReviewAPI
import com.theone.busandbt.databinding.FragmentShopReviewListBinding
import com.theone.busandbt.dialog.selection.ReviewSortTypeSelectionDialog
import com.theone.busandbt.dto.review.ReviewListItem
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.setOnReceiveData
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.adapter.review.ReviewListAdapter
import com.busandbt.code.ReviewSortType
import com.theone.busandbt.extension.safeApiRequest
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 전체리뷰보기 페이지
 */
class ShopReviewListFragment :
    SingleListFragment<FragmentShopReviewListBinding, ReviewListAdapter, ReviewListItem>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_shop_review_list
    override val actionBarTitle: String = "전체 리뷰보기"
    override val recyclerView: RecyclerView get() = binding.reviewListRecyclerView
    private val args by navArgs<ShopReviewListFragmentArgs>()
    private val reviewAPI: ReviewAPI by inject()
    private val reviewSortTypeSelectionDialog: ReviewSortTypeSelectionDialog by lazy { ReviewSortTypeSelectionDialog() }
    private var selectedReviewSortType = ReviewSortType.REGISTER_DATE

    override fun makeAdapter(): ReviewListAdapter = ReviewListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appbarChange()

        with(binding) {
            reviewImageToggleButton.isSelected = true
            safeApiRequest(
                reviewAPI.getReviewStatistics(args.shopId)
            ) {
                reviewStatistics = it
                it.rankedMenuList.forEachIndexed { index, s ->
                    when (index) {
                        0 -> firstPopularMenuNameTextView.text = s
                        1 -> secondPopularMenuNameTextView.text = s
                        2 -> thirdPopularMenuNameTextView.text = s
                    }
                }
                val reviewCountText =
                    if (it.reviewCount > 999) "999+" else it.reviewCount.toString()
                reviewCountTextView.text = reviewCountText
                val shopReplyCountText =
                    if (it.shopReplyCount > 999) "999+" else it.shopReplyCount.toString()
                shopReplyCountTextView.text = shopReplyCountText
            }
            reviewImageToggleButton.setOnClickListener {
                reviewImageToggleButton.isSelected = !reviewImageToggleButton.isSelected
                recyclerViewAdapter.isVisibleImage = reviewImageToggleButton.isSelected
                recyclerViewAdapter.refreshViews()
            }

            reviewSortTypeSelectionDialog.setOnReceiveData(this@ShopReviewListFragment) {
                if (it == null) return@setOnReceiveData
                selectedReviewSortType = it
                sortTextView.text = it.desc()
                refresh()
            }
            sortTextView.setOnClickListener {
                reviewSortTypeSelectionDialog.setDefault(selectedReviewSortType)
                reviewSortTypeSelectionDialog.show(childFragmentManager, null)
            }
        }
    }

    override fun onReceiveEmptyList(page: Int) {
        with(binding) {
            if (page == 1) {
                notExistGroup.isVisible = true
                coordinatorLayout.isVisible = false
            }
        }
    }

    override fun getCall(): Call<List<ReviewListItem>> =
        reviewAPI.getReviewList(
            page,
            15,
            shopId = args.shopId,
            reviewSortType = selectedReviewSortType.id,
            memberId = loginInfo?.id
        )

    /**
     * 스크롤 위치 감지해서 상단 뷰 바꿔줌.
     */
    private fun appbarChange() {
        with(binding) {
            appBar.addOnOffsetChangedListener { _, verticalOffset ->
                if (verticalOffset < -595) {
                    reviewImageToggleButton.visibility = View.GONE
                    photoTextView.visibility = View.GONE
                } else {
                    reviewImageToggleButton.visibility = View.VISIBLE
                    photoTextView.visibility = View.VISIBLE
                }
            }
        }
    }
}