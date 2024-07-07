package com.theone.busandbt.fragment.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.search.SearchResultListAdapter
import com.theone.busandbt.adapter.search.SearchShoppingResultListAdapter
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.SearchAPI
import com.theone.busandbt.databinding.FragmentSearchTotalResultBinding
import com.theone.busandbt.eventbus.DoInitPagerFragmentEvent
import com.theone.busandbt.eventbus.DoRefreshPagerFragmentEvent
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.menu.MallMenuDetailsFragmentArgs
import com.theone.busandbt.fragment.shop.ShopDetailFragmentArgs
import com.theone.busandbt.model.search.SearchResultViewModel
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject

/**
 * 전체 검색 결과 프래그먼트
 */
class SearchTotalResultFragment : DataBindingFragment<FragmentSearchTotalResultBinding>(),
    EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_search_total_result
    private val foodInstantListAdapter: SearchResultListAdapter by lazy {
        SearchResultListAdapter(
            DeliveryType.INSTANT
        )
    }
    private val foodPackagingListAdapter: SearchResultListAdapter by lazy {
        SearchResultListAdapter(
            DeliveryType.PACKAGING
        )
    }
    private val mallListAdapter: SearchShoppingResultListAdapter by lazy {
        SearchShoppingResultListAdapter(
            childFragmentManager
        )
    }
    private val viewModel: SearchResultViewModel by viewModels({ requireParentFragment() })
    private val searchAPI: SearchAPI by inject()
    private var keyword: String? = null
    private var isInit = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isInit = false
    }

    fun init() {
        if (isInit) return
        with(binding) {
            traditionRecycler.addItemDecoration(
                VerticalSpaceItemDecoration(
                    SizeUtils.dp2px(16f),
                    doFirst = false,
                    doLast = false
                )
            )
            deliveryRecycler.adapter = foodInstantListAdapter
            takeOutRecycler.adapter = foodPackagingListAdapter
            traditionRecycler.adapter = mallListAdapter
            moreDelivery.setOnClickListener {
                viewModel.setTabPosition(1)
            }
            moreTakeout.setOnClickListener {
                viewModel.setTabPosition(2)
            }
            moreMall.setOnClickListener {
                viewModel.setTabPosition(3)
            }
            foodInstantListAdapter.setOnItemClick { _, _, item ->
                view?.navigate(
                    R.id.shop_detail_graph,
                    ShopDetailFragmentArgs(
                        item.id,
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.INSTANT.id
                    ).toBundle()
                )
            }

            foodPackagingListAdapter.setOnItemClick { _, _, item ->
                view?.navigate(
                    R.id.shop_detail_graph,
                    ShopDetailFragmentArgs(
                        item.id,
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.PACKAGING.id
                    ).toBundle()
                )
            }

            mallListAdapter.setOnItemClick { _, _, item ->
                view?.navigate(
                    R.id.menu_detail_graph,
                    MallMenuDetailsFragmentArgs(
                        item.id,
                        item.shopId,
                        item.shopName,
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id,
                        0
                    ).toBundle()
                )
            }
            viewModel.keywordLiveData.observe(viewLifecycleOwner) {
                keyword = it
                refresh()
            }
            refresh()
        }
        isInit = true
    }

    fun refresh() {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
        val k = keyword ?: return
        safeApiRequest(
            searchAPI.getSearchAllList(da.jibun, k)
        ) {
            foodInstantListAdapter.setItems(it.foodInstantList)
            foodPackagingListAdapter.setItems(it.foodPackagingList)
            mallListAdapter.setItems(it.mallMenuList)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDoInitEvent(event: DoInitPagerFragmentEvent) {
        val args = arguments ?: return
        if (args.getInt("position") == event.position) init()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDoRefreshEvent(event: DoRefreshPagerFragmentEvent) {
        val args = arguments ?: return
        if (args.getInt("position") == event.position) refresh()
    }
}