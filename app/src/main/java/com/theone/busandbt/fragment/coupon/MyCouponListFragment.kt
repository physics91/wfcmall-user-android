package com.theone.busandbt.fragment.coupon

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.R
import com.theone.busandbt.adapter.coupon.MyCouponListAdapter
import com.theone.busandbt.api.orderchannel.CouponAPI
import com.theone.busandbt.databinding.FragmentMyCouponListBinding
import com.theone.busandbt.dto.coupon.MemberCouponListItem
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.SingleListFragment
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 내 쿠폰 리스트
 */
class MyCouponListFragment :
    SingleListFragment<FragmentMyCouponListBinding, MyCouponListAdapter, MemberCouponListItem>() {

    override val layoutId: Int = R.layout.fragment_my_coupon_list
    override val recyclerView: RecyclerView get() = binding.couponRecyclerView
    private val args by navArgs<MyCouponFragmentArgs>()
    private val couponAPI: CouponAPI by inject()

    override fun getCall(): Call<List<MemberCouponListItem>> {
        val innerLoginInfo = loginInfo ?: error("부적절한 접근")
        return couponAPI.getMemberCouponList(
            innerLoginInfo.getFormedToken(),
            page,
            15,
            memberId = innerLoginInfo.id,
            serviceType = args.serviceTypeId,
            deliveryType = args.deliveryTypeId,
        )
    }

    override fun makeAdapter(): MyCouponListAdapter = MyCouponListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val innerLoginInfo = loginInfo ?: throw IllegalStateException("부적절한 접근")
        with(binding) {
            safeApiRequest(
                couponAPI.getMemberCouponCount(
                    innerLoginInfo.getFormedToken(),
                    innerLoginInfo.id,
                    serviceType = args.serviceTypeId,
                    deliveryType = args.deliveryTypeId
                )
            ) {
                couponCountTextView.text = "보유쿠폰 ${it.couponCount}장"
                if (it.couponCount <= 0) notExistCouponInclude.root.isVisible = true
            }

        }
    }

    override fun onResume() {
        val innerLoginInfo = loginInfo ?: error("부적절한 접근")
        super.onResume()
        safeApiRequest(
            couponAPI.getMemberCouponList(
                innerLoginInfo.getFormedToken(),
                if (recyclerViewAdapter.items.size <= 15) 1 else page,
                15,
                memberId = innerLoginInfo.id,
                serviceType = args.serviceTypeId,
                deliveryType = args.deliveryTypeId,
            )
        ) {
            recyclerViewAdapter.setItems(it)
        }
    }

    override fun onReceiveEmptyList(page: Int) {
        if (page == 1) {
            binding.notExistCouponInclude.root.isVisible = true
        }
    }
}
