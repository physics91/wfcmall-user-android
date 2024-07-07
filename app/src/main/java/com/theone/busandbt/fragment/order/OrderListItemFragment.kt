package com.theone.busandbt.fragment.order

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.order.OrderListAdapter
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.databinding.FragmentOrderListChildBinding
import com.theone.busandbt.dto.order.OrderListItem
import com.theone.busandbt.eventbus.ChangeOrderStatusEvent
import com.theone.busandbt.eventbus.DoInitPagerFragmentEvent
import com.theone.busandbt.extension.getDeliveryType
import com.theone.busandbt.extension.getServiceType
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DoubleListFragment
import com.theone.busandbt.fragment.shop.ServiceMainFragmentArgs
import com.theone.busandbt.fragment.shop.ShopDetailFragmentArgs
import com.theone.busandbt.model.order.OrderListMainViewModel
import com.theone.busandbt.utils.ORDER_COMPLETE_STATUS_ARRAY
import com.theone.busandbt.utils.ORDER_COMPLETE_STATUS_ID_ARRAY
import com.theone.busandbt.utils.ORDER_PROCESS_STATUS_ID_ARRAY
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 주문내역 리스트 화면
 */
class OrderListItemFragment :
    DoubleListFragment<FragmentOrderListChildBinding>(), EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_order_list_child
    override val recyclerView: RecyclerView get() = binding.orderRecyclerView
    override val recyclerViewAdapter: OrderListAdapter by lazy { OrderListAdapter() }
    override val nestedScrollView: NestedScrollView get() = binding.nestedScrollView
    override val isAutoLoad: Boolean = false
    private val parentViewModel: OrderListMainViewModel by viewModels({ requireParentFragment() })
    private val orderProcessListAdapter: OrderListAdapter by lazy { OrderListAdapter() }
    private val serviceType: ServiceType by lazy { arguments.getServiceType() }
    private val deliveryType: DeliveryType by lazy { arguments.getDeliveryType() }
    private val orderAPI: OrderAPI by inject()
    private var isInit = false

    override fun getCall(): Call<List<OrderListItem>> {
        val innerLoginInfo = loginInfo ?: throw IllegalStateException("비정상적인 접근입니다.")
        return orderAPI.getOrderList(
            innerLoginInfo.getFormedToken(),
            page,
            15,
            serviceType = serviceType.id,
            deliveryType = deliveryType.id,
            memberId = innerLoginInfo.id,
            statusList = ORDER_COMPLETE_STATUS_ID_ARRAY.joinToString().trim()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isInit = false
    }

    override fun onReceiveNotEmptyList(page: Int, data: List<OrderListItem>) {
        with(binding) {
            notExistOrderForm.root.isVisible = false
        }
    }

    override fun onReceiveEmptyList(page: Int) {
        with(binding) {
            if (page == 1 && orderProcessListAdapter.itemCount == 0) {
                notExistOrderForm.root.isVisible = true
            }
        }
    }

    fun init() {
        if (isInit) return
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            orderRecyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(15F)))

            parentViewModel.writtenReviewOrderId.observe(viewLifecycleOwner) {
                if (it == null) return@observe
                recyclerViewAdapter.writtenReviewOrder(it)
            }
            orderProcessListAdapter.setOnItemClick { _, _, item ->
                view?.navigate(
                    R.id.shop_detail_graph,
                    ShopDetailFragmentArgs(item.shopId, serviceType.id, deliveryType.id).toBundle()
                )
            }
            recyclerViewAdapter.setOnItemClick { _, _, item ->
                view?.navigate(
                    R.id.shop_detail_graph,
                    ShopDetailFragmentArgs(item.shopId, serviceType.id, deliveryType.id).toBundle()
                )
            }
            notExistOrderForm.notExistGoToOrderTextView.setOnClickListener {
                view?.navigate(
                    R.id.service_main_graph,
                    ServiceMainFragmentArgs(serviceType.id, deliveryType.id).toBundle()
                )
            }
            safeApiRequest(
                orderAPI.getOrderList(
                    innerLoginInfo.getFormedToken(),
                    1,
                    100,
                    serviceType = serviceType.id,
                    deliveryType = deliveryType.id,
                    memberId = innerLoginInfo.id,
                    statusList = ORDER_PROCESS_STATUS_ID_ARRAY.joinToString().trim()
                ),
                onFail = { _, _ ->
                    notExistOrderForm.root.isVisible = true
                }
            ) {
                orderProcessListAdapter.clear()
                orderProcessRecyclerView.adapter = orderProcessListAdapter.apply {
                    addItems(it)
                }
                refresh()
            }
        }
        isInit = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeOrderStatus(event: ChangeOrderStatusEvent) {
        val orderProcess = orderProcessListAdapter.getItem(event.orderId)
        val order = recyclerViewAdapter.getItem(event.orderId)
        if (orderProcess != null) {
            if (event.orderStatus in ORDER_COMPLETE_STATUS_ARRAY) {
                orderProcessListAdapter.remove(orderProcess)
                recyclerViewAdapter.addItemWithSort(orderProcess.copy(status = event.orderStatus.id))
            } else {
                orderProcessListAdapter.setOrderStatus(event.orderId, event.orderStatus)
            }
        }
        if (order != null) {
            recyclerViewAdapter.setOrderStatus(event.orderId, event.orderStatus)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDoInitPagerFragment(event: DoInitPagerFragmentEvent) {
        val args = arguments ?: return
        if (args.getInt("position") == event.position) init()
    }
}