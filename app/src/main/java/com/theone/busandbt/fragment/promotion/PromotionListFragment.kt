package com.theone.busandbt.fragment.promotion

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.PromotionType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.promotion.PromotionListAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.PromotionAPI
import com.theone.busandbt.databinding.FragmentPromotionListBinding
import com.theone.busandbt.dto.promotion.PromotionListItem
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 이벤트 배너 모두 보기 화면
 * TODO: 배너상세보기로 이동 후 뒤로가기로 다시 왔을 때 리스트 계속 출력 되는 버그 있음.
 */
class PromotionListFragment :
    SingleListFragment<FragmentPromotionListBinding, PromotionListAdapter, PromotionListItem>(),
    EnabledGoBackButton {

    override val actionBarTitle: String = "모두보기"
    override val layoutId: Int = R.layout.fragment_promotion_list
    override val recyclerView: RecyclerView get() = binding.promotionListRecyclerView
    private val promotionAPI: PromotionAPI by inject()
    override fun getCall(): Call<List<PromotionListItem>> =
        promotionAPI.getPromotionList(page = page, promotionType = PromotionType.BANNER.id)

    override fun makeAdapter(): PromotionListAdapter = PromotionListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(16F)))
        recyclerViewAdapter.setOnItemClick { _, _, item ->
            val action =
                PromotionListFragmentDirections.actionPromotionListFragmentToPromotionDetailFragment(
                    item.id
                )
            findNavController().navigate(action)
        }
    }
}
