package com.theone.busandbt.fragment.order

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.order.OrderListPagerAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.databinding.FragmentOrderListBinding
import com.theone.busandbt.eventbus.DoInitPagerFragmentEvent
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.function.getTabPosition
import com.theone.busandbt.model.order.OrderListMainViewModel
import com.theone.busandbt.utils.DELIVERY_TYPE_TAB_TITLE_ARRAY
import org.greenrobot.eventbus.EventBus

/**
 * 주문내역 메인 화면
 * 배달유형 탭을 가지고 있고 탭에 따라 각기다른 [OrderListItemFragment]를 보여준다.
 */
class OrderListMainFragment : DataBindingFragment<FragmentOrderListBinding>(),
    EnabledGoBackButton, RequiredLogin {

    override val layoutId: Int = R.layout.fragment_order_list
    override val actionBarTitle: String = "주문 내역"
    private val viewModel: OrderListMainViewModel by viewModels()
    private val args by navArgs<OrderListMainFragmentArgs>()

    override fun onPause() {
        super.onPause()
        with(binding) {
            viewModel.savedTabPosition = orderListTabTitle.selectedTabPosition
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            orderListViewPager.offscreenPageLimit = DELIVERY_TYPE_TAB_TITLE_ARRAY.size
            orderListViewPager.adapter = OrderListPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(orderListTabTitle, orderListViewPager) { tab, position ->
                tab.text = DELIVERY_TYPE_TAB_TITLE_ARRAY[position]
            }.attach()

            orderListTabTitle.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    EventBus.getDefault().post(DoInitPagerFragmentEvent(tab.position))
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
            orderListTabTitle.eventRegistrationTabSelectedDifferenceFont()
            initTabPosition()
            orderListViewPager.post {
                EventBus.getDefault()
                    .post(DoInitPagerFragmentEvent(getStartTabPosition()))
            }
        }
    }

    override fun onResultDataReceived(resultData: Bundle) {
        if (!resultData.getBoolean("doWrittenReview", false)) return
        val orderId = resultData.getString("orderId") ?: return
        viewModel.setWrittenReviewOrderId(orderId)
    }

    /**
     * 탭 위치 초기화
     */
    private fun initTabPosition() {
        with(binding.orderListViewPager) {
            viewModel.tabPositionLiveData.observe(viewLifecycleOwner) {
                if (it != null) setCurrentItem(it, false)
            }
            doOnPreDraw {
                viewModel.setTabPosition(getStartTabPosition())
            }
        }
    }

    private fun getStartTabPosition() =
        viewModel.savedTabPosition ?: getTabPosition(args.serviceTypeId, args.deliveryTypeId)
}