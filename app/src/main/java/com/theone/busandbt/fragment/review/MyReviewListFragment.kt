package com.theone.busandbt.fragment.review

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.R
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.ReviewAPI
import com.theone.busandbt.databinding.FragmentMyReviewListBinding
import com.theone.busandbt.dto.review.MemberReviewListItem
import com.theone.busandbt.eventbus.DoReviewRemoveEvent
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.adapter.review.MyReviewListAdapter
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.extension.safeApiRequest
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 내가 쓴 리뷰 리스트 화면
 */
class MyReviewListFragment :
    SingleListFragment<FragmentMyReviewListBinding, MyReviewListAdapter, MemberReviewListItem>(),
    EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_my_review_list
    override val recyclerView: RecyclerView get() = binding.myReviewRecyclerView
    override val isAutoLoad: Boolean = false
    private val args by navArgs<MyReviewFragmentArgs>()
    private val reviewAPI: ReviewAPI by inject()

    override fun makeAdapter(): MyReviewListAdapter = MyReviewListAdapter(
        this,
        ServiceType.find(args.serviceTypeId),
        DeliveryType.find(args.deliveryTypeId)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
    }

    override fun getCall(): Call<List<MemberReviewListItem>> {
        return reviewAPI.getMemberReviewList(
            page,
            15,
            loginInfo?.id ?: 0,
            args.serviceTypeId,
            args.deliveryTypeId
        )
    }

    override fun onReceiveEmptyList(page: Int) {
        if (page == 1) {
            binding.notExistGroup.isVisible = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun doRemoveReview(event: DoReviewRemoveEvent) {
        val innerLoginInfo = loginInfo ?: return
        safeApiRequest(
            reviewAPI.removeReview(innerLoginInfo.getFormedToken(), event.review.id)
        ) {
            innerLoginInfo.reviewCount -= 1
            loginInfoViewModel.update()
            view?.showMessageBar("선택한 리뷰가 삭제되었어요.")
            recyclerViewAdapter.remove(event.review)
            if (recyclerViewAdapter.itemCount == 0) {
                binding.notExistGroup.isVisible = true
            }
        }
    }
}