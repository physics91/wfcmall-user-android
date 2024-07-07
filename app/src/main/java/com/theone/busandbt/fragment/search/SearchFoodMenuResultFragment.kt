package com.theone.busandbt.fragment.search

import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.R
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.FragmentSearchFoodMenuResultBinding
import com.theone.busandbt.dto.menu.FoodMenuListItem
import com.theone.busandbt.eventbus.ChangeSearchKeywordOrSortEvent
import com.theone.busandbt.eventbus.ClearSearchDataEvent
import com.theone.busandbt.extension.getDeliveryType
import com.theone.busandbt.extension.getServiceType
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.adapter.search.SearchFoodMenuListAdapter
import com.theone.busandbt.instance.MAIN_COLOR
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.fragment.menu.FoodMenuDetailFragmentArgs
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 상점 상세 내 메뉴 검색 결과 리스트 화면
 */
class SearchFoodMenuResultFragment :
    SingleListFragment<FragmentSearchFoodMenuResultBinding, SearchFoodMenuListAdapter, FoodMenuListItem>(),
    EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_search_food_menu_result
    override val isAutoLoad: Boolean = false
    override val recyclerView: RecyclerView get() = binding.foodMenuListRecyclerView
    private val serviceType: ServiceType by lazy { arguments.getServiceType() }
    private val deliveryType: DeliveryType by lazy { arguments.getDeliveryType() }
    private val menuAPI: MenuAPI by inject()
    private var keyword: String? = null

    override fun makeAdapter(): SearchFoodMenuListAdapter = SearchFoodMenuListAdapter()

    override fun getCall(): Call<List<FoodMenuListItem>> {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: error("비정상적인 접근입니다.")
        val args = arguments ?: error("비정상적인 접근입니다.")
        return menuAPI.getFoodMenuList(
            page,
            15,
            jibun = da.jibun,
            keyword = keyword,
            shopId = args.getInt("shopId"),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val args = arguments ?: return
            val shopId = args.getInt("shopId")
            val shopName = args.getString("shopName") ?: return
            val minOrderCost = args.getInt("minOrderCost")
            recyclerViewAdapter.setOnItemClick { view, _, item ->
                view.navigate(
                    R.id.food_menu_detail_graph,
                    FoodMenuDetailFragmentArgs(
                        item.id,
                        shopId,
                        shopName,
                        serviceType.id,
                        deliveryType.id,
                        minOrderCost
                    ).toBundle()
                )
            }
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
                        ForegroundColorSpan(MAIN_COLOR),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    append("에 대한 검색결과가 없어요.")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSearchKeywordOrSort(event: ChangeSearchKeywordOrSortEvent) {
        if (keyword != event.keyword) keyword = event.keyword
        binding.notExistSearchResult.root.isVisible = false
        refresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClearSearchData(event: ClearSearchDataEvent) {
        recyclerViewAdapter.clear()
        if (keyword != event.keyword) keyword = event.keyword
        onReceiveEmptyList(1)
    }
}