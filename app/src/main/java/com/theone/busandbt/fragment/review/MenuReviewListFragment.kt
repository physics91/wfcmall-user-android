package com.theone.busandbt.fragment.review

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.ReviewAPI
import com.theone.busandbt.databinding.FragmentSeeMenuReviewBinding
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
 * 메뉴별 리뷰보기 화면
 * TODO [ShopReviewListFragment]와 동일한 코드가 많아 공통으로 묶을 필요가 있다.
 */
class MenuReviewListFragment :
    SingleListFragment<FragmentSeeMenuReviewBinding, ReviewListAdapter, ReviewListItem>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_see_menu_review
    override val actionBarTitle: String = "메뉴별 리뷰보기"
    override val recyclerView: RecyclerView get() = binding.reviewListRecyclerView
    private val args by navArgs<MenuReviewListFragmentArgs>()
    private val reviewAPI: ReviewAPI by inject()
    private val reviewSortTypeSelectionDialog: ReviewSortTypeSelectionDialog by lazy { ReviewSortTypeSelectionDialog() }
    private var selectedReviewSortType = ReviewSortType.REGISTER_DATE

    override fun makeAdapter(): ReviewListAdapter = ReviewListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            reviewImageToggleButton.isSelected = true
            menuName = args.menuName
            safeApiRequest(
                reviewAPI.getReviewCountByMenu(args.menuId)
            ) {
                val reviewCountText =
                    if (it.reviewCount > 999) "999+" else it.reviewCount.toString()
                reviewCountTextView.text = reviewCountText
                val shopReplyCountText =
                    if (it.shopReplyCount > 999) "999+" else it.shopReplyCount.toString()
                shopReplyCountTextView.text = shopReplyCountText
            }

            reviewSortTypeSelectionDialog.setOnReceiveData(this@MenuReviewListFragment) {
                if (it == null) return@setOnReceiveData
                selectedReviewSortType = it
                reviewSortTypeSelection.text = it.desc()
                refresh()
            }

            reviewSortTypeSelection.setOnClickListener {
                reviewSortTypeSelectionDialog.setDefault(selectedReviewSortType)
                reviewSortTypeSelectionDialog.show(childFragmentManager, null)
            }
            reviewImageToggleButton.setOnClickListener {
                reviewImageToggleButton.isSelected = !reviewImageToggleButton.isSelected
                recyclerViewAdapter.isVisibleImage = reviewImageToggleButton.isSelected
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun getCall(): Call<List<ReviewListItem>> =
        reviewAPI.getReviewListByMenu(
            page,
            15,
            menuId = args.menuId,
            reviewSortType = selectedReviewSortType.id,
            memberId = loginInfo?.id
        )
}