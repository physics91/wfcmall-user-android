package com.theone.busandbt.fragment.search

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.MenuSortType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.search.SearchShoppingResultListAdapter
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.FragmentSearchShoppingResultBinding
import com.theone.busandbt.dto.menu.MallMenuListItem
import com.theone.busandbt.eventbus.ClearSearchDataEvent
import com.theone.busandbt.eventbus.DoInitPagerFragmentEvent
import com.theone.busandbt.eventbus.DoRefreshPagerFragmentEvent
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.fragment.menu.MallMenuDetailsFragmentArgs
import com.theone.busandbt.model.search.SearchResultViewModel
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 쇼핑몰 검색 결과
 */
class SearchShoppingResultFragment : SingleListFragment<FragmentSearchShoppingResultBinding,
        SearchShoppingResultListAdapter, MallMenuListItem>(
), EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_search_shopping_result
    override val isAutoLoad: Boolean = false
    override val recyclerView: RecyclerView get() = binding.shoppingListRecyclerView
    private val searchResultViewModel: SearchResultViewModel by viewModels({ requireParentFragment() })
    private val menuAPI: MenuAPI by inject()
    private var keyword: String? = null
    private var coupon: Boolean? = null
    private var menuSortType: MenuSortType? = null
    private var isInit = false

    override fun makeAdapter(): SearchShoppingResultListAdapter = SearchShoppingResultListAdapter(childFragmentManager)

    override fun getCall(): Call<List<MallMenuListItem>> {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: error("비정상적인 접근입니다.")
        return menuAPI.getMallMenuList(
            page,
            15,
            jibun = da.jibun,
            keyword = keyword,
            coupon = coupon,
            menuSortType = menuSortType?.id
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isInit = false
    }

    fun init() {
        debugLog("허허", "1")
        if (isInit) return
        debugLog("허허", "2")
        with(binding) {
            searchResultViewModel.keywordLiveData.observe(viewLifecycleOwner) { keyword ->
                this@SearchShoppingResultFragment.keyword = keyword
                if (isInit) refresh()
            }
            hideKeyboard(requireContext())
            recyclerViewAdapter.setOnItemClick { v, _, item ->
                v.navigate(
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
            recyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(21F)))
            recyclerView.addItemDecoration(
                VerticalSpaceItemDecoration(
                    SizeUtils.dp2px(16f),
                    doFirst = false,
                    doLast = false
                )
            )
            searchResultViewModel.hasCouponLiveData.observe(viewLifecycleOwner) {
                coupon = it
            }
            searchResultViewModel.keywordLiveData.observe(viewLifecycleOwner) {
                keyword = it
            }
            searchResultViewModel.menuSortTypeLiveData.observe(viewLifecycleOwner) {
                menuSortType = it
            }
            refresh()
        }
        isInit = true
    }

    override fun onReceiveNotEmptyList(page: Int, data: List<MallMenuListItem>) {
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

    fun hideKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = requireActivity().currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onChangeSearchKeywordOrSort(event: ChangeSearchKeywordOrSortEvent) {
//        var changed = false
//        if (keyword != event.keyword) {
//            keyword = event.keyword
//            changed = true
//        }
////        if (menuSortType != event.shopSortType) {
////            menuSortType = event.shopSortType
////            changed = true
////        }
//        binding.notExistSearchResult.root.isVisible = false
//        refresh()
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClearSearchData(event: ClearSearchDataEvent) {
        recyclerViewAdapter.clear()
        if (keyword != event.keyword) {
            keyword = event.keyword
        }
        onReceiveEmptyList(1)
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