package com.theone.busandbt.fragment.search

import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.busandbt.code.ShopSortType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.search.SearchResultListAdapter
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.FragmentSearchFoodShopResultBinding
import com.theone.busandbt.dto.shop.ShopListItem
import com.theone.busandbt.eventbus.DoInitPagerFragmentEvent
import com.theone.busandbt.eventbus.DoRefreshPagerFragmentEvent
import com.theone.busandbt.extension.getDeliveryType
import com.theone.busandbt.extension.getServiceType
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.fragment.shop.ShopDetailFragmentArgs
import com.theone.busandbt.model.search.SearchResultViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 음식점 검색 결과
 */
class SearchFoodShopResultFragment :
    SingleListFragment<FragmentSearchFoodShopResultBinding, SearchResultListAdapter, ShopListItem>(),
    EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_search_food_shop_result
    override val isAutoLoad: Boolean = false
    override val recyclerView: RecyclerView get() = binding.foodShopListRecyclerView
    private val searchResultViewModel: SearchResultViewModel by viewModels({ requireParentFragment() })
    private val serviceType: ServiceType by lazy { arguments.getServiceType() }
    private val deliveryType: DeliveryType by lazy { arguments.getDeliveryType() }
    private val shopAPI: ShopAPI by inject()
    private var keyword: String? = null
    private var coupon: Boolean? = null
    private var packaging: Boolean? = null
    private var minOrderCost: Int? = null
    private var shopSortType: ShopSortType? = ShopSortType.NEAR
    private var isInit = false
    private var distance: Double? = null

    override fun makeAdapter(): SearchResultListAdapter = SearchResultListAdapter(deliveryType)

    override fun getCall(): Call<List<ShopListItem>> {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: error("비정상적인 접근입니다.")
        return shopAPI.getShopList(
            page,
            15,
            jibun = da.jibun,
            keyword = keyword,
            serviceType = serviceType.id,
            deliveryType = deliveryType.id,
            coupon = coupon,
            packaging = packaging,
            minOrderCost = if (minOrderCost == 0) null else minOrderCost,
            distance = distance,
            customerLat = if (distance != null || shopSortType?.id == ShopSortType.NEAR.id) da.lat else null,
            customerLng = if (distance != null || shopSortType?.id == ShopSortType.NEAR.id) da.lng else null,
            shopSortType = shopSortType?.id
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isInit = false
    }

    fun init() {
        if (isInit) return
        with(binding) {
            searchResultViewModel.keywordLiveData.observe(viewLifecycleOwner) { keyword ->
                this@SearchFoodShopResultFragment.keyword = keyword
                if (isInit) refresh()
            }
            recyclerViewAdapter.setOnItemClick { view, _, item ->
                view.navigate(
                    R.id.shop_detail_graph,
                    ShopDetailFragmentArgs(item.id, serviceType.id, deliveryType.id).toBundle()
                )
            }
            searchResultViewModel.canPackagingLiveData.observe(viewLifecycleOwner) {
                packaging = it
            }
            searchResultViewModel.hasCouponLiveData.observe(viewLifecycleOwner) {
                coupon = it
            }
            searchResultViewModel.keywordLiveData.observe(viewLifecycleOwner) {
                keyword = it
            }
            searchResultViewModel.shopSortTypeLiveData.observe(viewLifecycleOwner) {
                shopSortType = it
            }
            searchResultViewModel.minOrderCostLiveData.observe(viewLifecycleOwner) {
                minOrderCost = it
            }
            refresh()
        }
        isInit = true
    }

    override fun onReceiveNotEmptyList(page: Int, data: List<ShopListItem>) {
        with(binding) {
            notExistSearchResult.root.isVisible = false
        }
    }

    override fun onReceiveEmptyList(page: Int) {
        if (page == 1) {
            with(binding) {
                CommonBindingAdapter.centerVerticalInWindow(
                    notExistSearchResult.notExistDataImageView,
                    true
                )
                notExistSearchResult.root.isVisible = true
                val k = keyword ?: ""
                notExistSearchResult.mainTitleTextView.text = buildSpannedString {
                    append("검색하신 ")
                    val showKeyword =
                        if (k.length > 20) "${k.substring(0..20)}..." else k
                    append(
                        showKeyword,
                        ForegroundColorSpan(ColorUtils.getColor(R.color.mainColor)),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    append("에 대한 검색결과가 없어요.")
                }
            }
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