package com.theone.busandbt.fragment.order

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.order.MallOrderListAdapter
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.databinding.FragmentShoppingOrderListItemBinding
import com.theone.busandbt.dto.order.MallOrderListItem
import com.theone.busandbt.eventbus.DoInitPagerFragmentEvent
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.fragment.shop.ServiceMainFragmentArgs
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import retrofit2.Call


/**
 * 쇼핑몰 주문내역 아이템 프래그먼트
 */
class MallOrderListItemFragment :
    SingleListFragment<FragmentShoppingOrderListItemBinding, MallOrderListAdapter, MallOrderListItem>() {
    override val layoutId: Int = R.layout.fragment_shopping_order_list_item
    override val recyclerView: RecyclerView get() = binding.orderListRecyclerView
    override val isAutoLoad: Boolean = false
    private val orderAPI: OrderAPI by inject()
    private var isInit = false

    override fun getCall(): Call<List<MallOrderListItem>> {
        val innerLoginInfo = loginInfo ?: error("비정상적인 접근입니다.")
        return orderAPI.getMallOrderList(
            innerLoginInfo.getFormedToken(),
            page,
            15,
            memberId = innerLoginInfo.id,
        )
    }

    override fun makeAdapter(): MallOrderListAdapter = MallOrderListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isInit = false
    }

    fun init() {
        if (isInit) return
        with(binding) {
            orderListRecyclerView.addItemDecoration(
                HorizontalSpaceItemDecoration(
                    SizeUtils.dp2px(
                        16F
                    )
                )
            )
            notExistOrderForm.notExistGoToOrderTextView.setOnClickListener {
                view?.navigate(
                    R.id.service_main_graph,
                    ServiceMainFragmentArgs(
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id
                    ).toBundle()
                )
            }
            refresh()
        }
        isInit = true
    }

    override fun onReceiveNotEmptyList(page: Int, data: List<MallOrderListItem>) {
        with(binding) {
            notExistOrderForm.root.isVisible = false
        }
    }

    override fun onReceiveEmptyList(page: Int) {
        if (page == 1) {
            binding.notExistOrderForm.root.isVisible = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDoInitPagerFragment(event: DoInitPagerFragmentEvent) {
        val args = arguments ?: return
        if (args.getInt("position") == event.position) init()
    }
}