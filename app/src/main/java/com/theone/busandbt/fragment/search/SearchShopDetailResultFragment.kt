package com.theone.busandbt.fragment.search

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.theone.busandbt.R
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.databinding.FragmentSearchShopDetailResultBinding
import com.theone.busandbt.eventbus.ChangeSearchKeywordOrSortEvent
import com.theone.busandbt.eventbus.ChangeSearchTabPositionEvent
import com.theone.busandbt.eventbus.ClearSearchDataEvent
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.adapter.search.SearchDetailResultAdapter
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.item.RecentSearchKeyword
import com.theone.busandbt.model.RecentSearchKeywordViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import kotlin.properties.Delegates

/**
 * 상점,쇼핑몰 상세에서 검색결과 프래그먼트
 */
class SearchShopDetailResultFragment :
    DataBindingFragment<FragmentSearchShopDetailResultBinding>(), EventBusSubscriber,
    RequiredDeliveryAddress {
    override val layoutId: Int = R.layout.fragment_search_shop_detail_result
    private var searchedCount: Int by Delegates.observable(0) { _, _, newValue ->
        binding.mainSearchShadow.setText(buildSpannedString {
            append(keyword)
            append(
                " ${newValue}개",
                ForegroundColorSpan(Color.parseColor("#949494")),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }, TextView.BufferType.SPANNABLE)
    }
    private val args by navArgs<SearchShopDetailResultFragmentArgs>()
    private val menuAPI: MenuAPI by inject()
    private var keyword: String = ""
    private lateinit var recentSearchKeywordViewModel: RecentSearchKeywordViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            recentSearchKeywordViewModel = ViewModelProvider(
                requireActivity(),
                RecentSearchKeywordViewModel.Factory(requireActivity().application)
            )[RecentSearchKeywordViewModel::class.java]
            keyword = args.keyword
            mainSearch.setText(keyword)
            searchFocusStatus()
            debugLog("배달유형2", "${args.deliveryTypeId}")
            searchListViewPager.adapter =
                SearchDetailResultAdapter(
                    childFragmentManager,
                    lifecycle,
                    args.shopId,
                    args.shopName,
                    args.minOrderCost,
                    args.deliveryTypeId
                )
            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }
            keywordSearchButton.setOnClickListener {
                keyword = mainSearch.text.toString()
                if (keyword.isEmpty()) return@setOnClickListener
                search()
            }
            search()
        }
    }

    private fun search() {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
        recentSearchKeywordViewModel.addSearch(RecentSearchKeyword(keyword = keyword))
        safeApiRequest(
            menuAPI.getMenuCount(
                jibun = da.jibun,
                keyword = keyword,
                serviceType = args.serviceTypeId,
                shopId = args.shopId.takeIf { it > 0 }
            )
        ) {
            afterGetSearchDataCount(it.count)
        }
    }

    /**
     * 검색 개수를 조회 후 로직을 처리한다.
     */
    private fun afterGetSearchDataCount(menuCount: Int) {
        with(binding) {
            searchedCount = menuCount
            if (menuCount != 0) {
                EventBus.getDefault().post(
                    ChangeSearchKeywordOrSortEvent(
                        tabPosition = 0,
                        keyword = keyword
                    )
                )
            } else {
                EventBus.getDefault().post(ClearSearchDataEvent(0, keyword))
            }
            mainSearch.clearFocus()
            root.requestFocus()
        }
    }

    //검색창 포커스 여부에 따라 색상을 변경하는 함수
    private fun searchFocusStatus() {
        with(binding.mainSearch) {
            onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        setBackgroundResource(R.drawable.bg_address_focus_edittext)
                        val textLength = text?.length ?: 0
                        setSelection(textLength)
                        binding.mainSearchShadow.isVisible = false
                    } else {
                        binding.mainSearchShadow.setText(buildSpannedString {
                            append(keyword)
                            append(
                                " ${searchedCount}개",
                                ForegroundColorSpan(Color.parseColor("#949494")),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }, TextView.BufferType.SPANNABLE)
                        setBackgroundResource(R.drawable.bg_address_edittext_selector)
                        binding.mainSearchShadow.isVisible = true
                    }
                }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSearchTabPosition(event: ChangeSearchTabPositionEvent) {
        with(binding) {
            searchListViewPager.currentItem = event.tabPosition
        }
    }
}