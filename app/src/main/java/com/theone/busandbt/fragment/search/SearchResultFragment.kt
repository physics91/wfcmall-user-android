package com.theone.busandbt.fragment.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.busandbt.code.MenuSortType
import com.busandbt.code.ShopSortType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.search.SearchResultPagerAdapter
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.databinding.FragmentSearchResultBinding
import com.theone.busandbt.dialog.selection.MenuSortTypeSelectionDialog
import com.theone.busandbt.dialog.selection.MinOrderCostSelectionDialog
import com.theone.busandbt.dialog.selection.ShopSortTypeSelectionDialog
import com.theone.busandbt.dto.Selection
import com.theone.busandbt.eventbus.DoInitPagerFragmentEvent
import com.theone.busandbt.eventbus.DoRefreshPagerFragmentEvent
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.setOnReceiveData
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.instance.MAIN_TEXT_COLOR
import com.theone.busandbt.item.RecentSearchKeyword
import com.theone.busandbt.model.RecentSearchKeywordViewModel
import com.theone.busandbt.model.search.SearchResultViewModel
import com.theone.busandbt.type.MinOrderCostType
import org.greenrobot.eventbus.EventBus

/**
 * 검색 결과 프래그먼트
 */
class SearchResultFragment :
    DataBindingFragment<FragmentSearchResultBinding>(), EventBusSubscriber,
    RequiredDeliveryAddress {
    override val layoutId: Int = R.layout.fragment_search_result
    private val args by navArgs<SearchResultFragmentArgs>()
    private val viewModel: SearchResultViewModel by viewModels()
    private var keyword: String = ""
    private var coupon: Boolean? = null
    private var packaging: Boolean? = null
    private val shopSortTypeInputList: List<Selection<ShopSortType>> by lazy {
        ShopSortType.values().filter { it.id > 0 }.map { Selection(it.desc(), it) }
    }
    private val shopSortTypeSelectionDialog: ShopSortTypeSelectionDialog by lazy {
        ShopSortTypeSelectionDialog(
            shopSortTypeInputList
        )
    }
    private val menuSortTypeInputList: List<Selection<MenuSortType>> by lazy {
        MenuSortType.values().filter { it.id > 0 }.map { Selection(it.desc(), it) }
    }
    private val menuSortTypeSelectionDialog: MenuSortTypeSelectionDialog by lazy {
        MenuSortTypeSelectionDialog(
            menuSortTypeInputList
        )
    }
    private var selectedShopSortType: ShopSortType = ShopSortType.NEAR
    private var selectedMenuSortType: MenuSortType = MenuSortType.MANY_ORDER
    private val minOrderCostTypeSelectionDialog: MinOrderCostSelectionDialog by lazy {
        MinOrderCostSelectionDialog()
    }
    private var selectedMinOrderCost: MinOrderCostType = MinOrderCostType.ALL
    private lateinit var recentSearchKeywordViewModel: RecentSearchKeywordViewModel

    private val tabTitleArray = arrayOf(
        "전체",
        "배달",
        "포장",
        "택배배송"
    )

    override fun onPause() {
        super.onPause()
        with(binding) {
            viewModel.savedTabPosition = tabTitle.selectedTabPosition
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            hideKeyboard(requireContext())
            recentSearchKeywordViewModel = ViewModelProvider(
                requireActivity(),
                RecentSearchKeywordViewModel.Factory(requireActivity().application)
            )[RecentSearchKeywordViewModel::class.java]
            keyword = args.keyword
            mainSearch.setText(keyword)
            searchFocusStatus()

            searchListViewPager.offscreenPageLimit = tabTitleArray.size
            searchListViewPager.adapter =
                SearchResultPagerAdapter(this@SearchResultFragment, tabTitleArray)
            TabLayoutMediator(tabTitle, searchListViewPager) { tab, position ->
                tab.text = tabTitleArray[position]
            }.attach()

            tabTitle.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    EventBus.getDefault().post(DoInitPagerFragmentEvent(tab.position))
                    when (tab.position) {
                        0 -> {
                            resetImageView.isVisible = false
                            shopSortTypeTextView.isVisible = false
                            menuSortTypeTextView.isVisible = false
                            minOrderCostTextView.isVisible = false
                            couponBadgeTextView.isVisible = false
                            packagingBadgeTextView.isVisible = false
                        }

                        1, 2 -> {
                            shopSortTypeTextView.isVisible = true
                            menuSortTypeTextView.isVisible = false
                            minOrderCostTextView.isVisible = true
                            couponBadgeTextView.isVisible = true
                            packagingBadgeTextView.isVisible = true
                            setInitFilterButton()
                        }

                        3 -> {
                            shopSortTypeTextView.isVisible = false
                            menuSortTypeTextView.isVisible = true
                            minOrderCostTextView.isVisible = false
                            couponBadgeTextView.isVisible = true
                            packagingBadgeTextView.isVisible = false
                            setInitFilterButton()
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }

            resetImageView.setOnClickListener {
                resetImageView.isVisible = false
                this@SearchResultFragment.coupon = null
                packaging = null
                selectedMinOrderCost = MinOrderCostType.ALL
                minOrderCostTextView.text = "최소주문금액"
                minOrderCostTextView.setBackgroundResource(R.drawable.bg_common_sort)
                minOrderCostTextView.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
                minOrderCostTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow,
                    0
                )
                selectedShopSortType = ShopSortType.NEAR
                shopSortTypeTextView.text = "가까운 순"
                shopSortTypeTextView.setBackgroundResource(R.drawable.bg_common_sort)
                shopSortTypeTextView.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
                shopSortTypeTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow,
                    0
                )
                selectedMenuSortType = MenuSortType.MANY_ORDER
                couponBadgeTextView.setBackgroundResource(R.drawable.bg_common_sort)
                couponBadgeTextView.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
                couponBadgeTextView.isSelected = false
                packagingBadgeTextView.setBackgroundResource(R.drawable.bg_common_sort)
                packagingBadgeTextView.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
                packagingBadgeTextView.isSelected = false
                minOrderCostTextView.isSelected = false
                setInitFilterButton()
                viewModel.setHasCoupon(coupon)
                viewModel.setCanPackaging(packaging)
                viewModel.setShopSortType(selectedShopSortType)
                viewModel.setMenuSortType(selectedMenuSortType)
                viewModel.setMinOrdercost(selectedMinOrderCost.minOrderCost)
                refreshPagerFragment(1)
                refreshPagerFragment(2)
                refreshPagerFragment(3)
            }

            couponBadgeTextView.setOnClickListener {
                val currentState = couponBadgeTextView.isSelected
                couponBadgeTextView.setTextColor(if (currentState) MAIN_TEXT_COLOR else MAIN_COLOR)
                couponBadgeTextView.setBackgroundResource(if (currentState) R.drawable.bg_filter_item else R.drawable.bg_address_choice_text)
                coupon = if (currentState) null else true
                couponBadgeTextView.isSelected = !couponBadgeTextView.isSelected
                setInitFilterButton()
                viewModel.setHasCoupon(coupon)
                refreshPagerFragment(1)
                refreshPagerFragment(2)
                refreshPagerFragment(3)
            }

            packagingBadgeTextView.setOnClickListener {
                val currentState = packagingBadgeTextView.isSelected
                packagingBadgeTextView.setTextColor(if (currentState) MAIN_TEXT_COLOR else MAIN_COLOR)
                packagingBadgeTextView.setBackgroundResource(if (currentState) R.drawable.bg_filter_item else R.drawable.bg_address_choice_text)
                packaging = if (currentState) null else true
                packagingBadgeTextView.isSelected = !packagingBadgeTextView.isSelected
                setInitFilterButton()
                viewModel.setCanPackaging(packaging)
                refreshPagerFragment(1)
                refreshPagerFragment(2)
            }

            shopSortTypeTextView.setOnClickListener {
                shopSortTypeSelectionDialog.setDefault(selectedShopSortType)
                shopSortTypeSelectionDialog.show(childFragmentManager, null)
            }

            menuSortTypeTextView.setOnClickListener {
                menuSortTypeSelectionDialog.setDefault(selectedMenuSortType)
                menuSortTypeSelectionDialog.show(childFragmentManager, null)
            }

            minOrderCostTextView.setOnClickListener {
                minOrderCostTypeSelectionDialog.setDefault(selectedMinOrderCost)
                minOrderCostTypeSelectionDialog.show(childFragmentManager, null)
            }

            keywordSearchButton.setOnClickListener {
                keyword = mainSearch.text.toString()
                if (keyword.isEmpty()) return@setOnClickListener
                viewModel.setKeyword(keyword)
                KeyboardUtils.hideSoftInput(mainSearch)
                recentSearchKeywordViewModel.addSearch(RecentSearchKeyword(keyword = keyword))
            }

            mainSearchShadow.setOnClickListener {
                mainSearch.requestFocus()
                KeyboardUtils.showSoftInput(mainSearch)
            }
            receiveDialogData()
            initTabPosition()
            searchListViewPager.post {
                EventBus.getDefault().post(DoInitPagerFragmentEvent(viewModel.savedTabPosition))
            }
        }
    }

    private fun receiveDialogData() {
        with(binding) {
            minOrderCostTypeSelectionDialog.setOnReceiveData(this@SearchResultFragment) {
                if (it == null) return@setOnReceiveData
                selectedMinOrderCost = it
                minOrderCostTextView.text = it.title
                minOrderCostTextView.setBackgroundResource(R.drawable.bg_address_choice_text)
                minOrderCostTextView.setTextColor(MAIN_COLOR)
                minOrderCostTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow_main_color,
                    0
                )
                setInitFilterButton()
                viewModel.setMinOrdercost(it.minOrderCost)
                refreshPagerFragment(1)
                refreshPagerFragment(2)
            }

            shopSortTypeSelectionDialog.setOnReceiveData(this@SearchResultFragment) {
                if (it == null) return@setOnReceiveData
                selectedShopSortType = it
                shopSortTypeTextView.text = it.desc()
                shopSortTypeTextView.setBackgroundResource(R.drawable.bg_address_choice_text)
                shopSortTypeTextView.setTextColor(MAIN_TEXT_COLOR)
                shopSortTypeTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow_main_color,
                    0
                )
                setInitFilterButton()
                viewModel.setShopSortType(it)
                refreshPagerFragment(1)
                refreshPagerFragment(2)
            }

            menuSortTypeSelectionDialog.setOnReceiveData(this@SearchResultFragment) {
                if (it == null) return@setOnReceiveData
                selectedMenuSortType = it
                menuSortTypeTextView.text = it.desc()
                menuSortTypeTextView.setBackgroundResource(R.drawable.bg_address_choice_text)
                menuSortTypeTextView.setTextColor(MAIN_TEXT_COLOR)
                menuSortTypeTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow_main_color,
                    0
                )
                setInitFilterButton()
                viewModel.setMenuSortType(it)
                refreshPagerFragment(3)
            }
        }
    }

    /**
     * 현재 필터 상태에 따라 초기화 버튼의 상태를 지정한다.
     */
    private fun setInitFilterButton() {
        with(binding) {
            val flag =
                selectedMinOrderCost != MinOrderCostType.ALL
                        || selectedShopSortType != ShopSortType.NEAR
                        || couponBadgeTextView.isSelected
                        || packagingBadgeTextView.isSelected
                        || selectedMenuSortType != MenuSortType.MANY_ORDER
            resetImageView.isVisible = flag
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
                    } else {
                        setBackgroundResource(R.drawable.bg_address_edittext_selector)
                    }
                }
        }
    }

    private fun hideKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = requireActivity().currentFocus
        if (currentFocusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
        }
    }

    /**
     * 탭 위치 초기화
     */
    private fun initTabPosition() {
        with(binding.searchListViewPager) {
            viewModel.tabPositionLiveData.observe(viewLifecycleOwner) {
                if (it != null) setCurrentItem(it, false)
            }
            doOnPreDraw {
                viewModel.setTabPosition(viewModel.savedTabPosition)
            }
        }
    }

    private fun refreshPagerFragment(position: Int) {
        EventBus.getDefault()
            .postSticky(DoRefreshPagerFragmentEvent(position))
    }
}