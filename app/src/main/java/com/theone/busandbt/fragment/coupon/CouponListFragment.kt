package com.theone.busandbt.fragment.coupon

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.busandbt.code.CouponType
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.coupon.CouponListAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.CouponAPI
import com.theone.busandbt.databinding.FragmentCouponListBinding
import com.theone.busandbt.dto.coupon.CouponEvent
import com.theone.busandbt.dto.coupon.MemberCouponByShop
import com.theone.busandbt.dto.coupon.MemberCouponListItem
import com.theone.busandbt.dto.coupon.UseCoupon
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.fragment.SingleListFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 쿠폰 리스트 화면
 * 상점이 발행하고 있는 쿠폰 리스트를 보여준다.
 */
class CouponListFragment :
    SingleListFragment<FragmentCouponListBinding, CouponListAdapter, MemberCouponByShop>(),
    EnabledGoBackButton, EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_coupon_list
    override val actionBarTitle: String = "쿠폰"
    override val recyclerView: RecyclerView get() = binding.couponList
    private val args by navArgs<CouponListFragmentArgs>()
    private val serviceType: ServiceType by lazy { ServiceType.find(args.serviceTypeId) }
    private val deliveryType: DeliveryType by lazy { DeliveryType.find(args.deliveryTypeId) }
    private val couponAPI: CouponAPI by inject()
    private var currentCost = 0
    private val couponSelectItemList = ArrayList<MemberCouponListItem>()

    override fun getCall(): Call<List<MemberCouponByShop>> {
        val innerLoginInfo = loginInfo ?: error("부적절한 접근")
        return couponAPI.getMemberCouponByShopList(
            innerLoginInfo.getFormedToken(),
            page,
            15,
            memberId = innerLoginInfo.id,
            serviceType = serviceType.id,
            deliveryType = deliveryType.id,
            couponType = args.couponTypeId,
            shopIdList = if (args.couponTypeId == CouponType.SHOP.id) args.shopIdList?.toList() else null,
            paymentCost = args.paymentCost.takeIf { it > 0 },
            otherCouponIdList = args.couponIdList?.toList()
        )
    }

    override fun makeAdapter(): CouponListAdapter = CouponListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val innerLoginInfo = loginInfo ?: return
            safeApiRequest(
                couponAPI.getMemberCouponCount(
                    innerLoginInfo.getFormedToken(),
                    innerLoginInfo.id,
                    serviceType.id,
                    deliveryType.id,
                    args.couponTypeId,
                    paymentCost = args.paymentCost.takeIf { it > 0 }
                )
            ) {
                possessionCouponTextView.text = "보유쿠폰 ${it.couponCount}장"
            }
            args.couponIdList?.let { Log.d("쿠폰 테스트1", it.joinToString()) }
            doneButton.setOnClickListener {
                val couponIds = ArrayList<Int>()
                val discountCosts = ArrayList<Int>()

                val couponList = couponSelectItemList.map {
                    UseCoupon(it.shopId, it.id, it.name, it.discountCost,it.type)
                }
                couponSelectItemList.forEach { item ->
                    couponIds.add(item.id)
                    discountCosts.add(item.discountCost)
                }

                setFragmentResult(
                    "selectOrderCoupon",
                    bundleOf(
                        "useCouponList" to couponList.toTypedArray(),
                        "couponTypeId" to args.couponTypeId,
                    )
                )
                findNavController().navigateUp()
            }
        }
    }

    override fun onReceiveEmptyList(page: Int) {
        if (page == 1) {
            binding.notExistCouponInclude.root.isVisible = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeCouponCost(event: CouponEvent) {
        if (event.isSelected) {
            currentCost += event.couponItem.discountCost
            couponSelectItemList.add(event.couponItem)
        } else {
            currentCost -= event.couponItem.discountCost
            couponSelectItemList.remove(event.couponItem)
        }
        binding.doneButton.text = "총 ${currentCost.toCommonMoneyForm()} 할인 적용"
    }
}