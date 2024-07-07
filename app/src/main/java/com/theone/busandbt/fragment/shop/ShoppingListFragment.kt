package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.CategoryType
import com.busandbt.code.DeliveryType
import com.busandbt.code.MenuSortType
import com.busandbt.code.ServiceType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.promotion.BannerViewPagerAdapter
import com.theone.busandbt.adapter.shop.MallMainCategoryTabAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EnabledGoBasketButton
import com.theone.busandbt.api.orderchannel.PromotionAPI
import com.theone.busandbt.databinding.FragmentShoppingListBinding
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.eventbus.menu.DoInitMallMenuListPagerFragmentEvent
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.extension.fixedPopBackStack
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.basket.BasketMainFragmentArgs
import com.theone.busandbt.fragment.like.LikeListFragmentArgs
import com.theone.busandbt.fragment.order.OrderListMainFragmentArgs
import com.theone.busandbt.fragment.promotion.PromotionDetailFragmentArgs
import com.theone.busandbt.fragment.search.SearchShopDetailMainFragmentArgs
import com.theone.busandbt.model.BannerViewModel
import com.theone.busandbt.model.category.CategoryViewModel
import com.theone.busandbt.model.menu.MallMenuListViewModel
import com.theone.busandbt.model.menu.MallMenuMainCategoryViewModel
import com.theone.busandbt.type.PromotionScreenType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * 바로배달 메인 프래그먼트
 */
class ShoppingListFragment :
    DataBindingFragment<FragmentShoppingListBinding>(), EnabledGoBackButton,
    EnabledGoBasketButton {
    override val layoutId: Int = R.layout.fragment_shopping_list
    private val args by navArgs<ShoppingListFragmentArgs>()
    private val categoryViewModel: CategoryViewModel by activityViewModel()
    private val promotionAPI: PromotionAPI by inject()
    private lateinit var bannerViewPagerAdapter: BannerViewPagerAdapter
    private lateinit var bannerViewModel: BannerViewModel
    private val viewModel: MallMenuListViewModel by activityViewModels()
    private val mainCategoryViewModel: MallMenuMainCategoryViewModel by viewModels()
    private var isRunning = true
    private var isInit = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isInit = false
        init()
    }

    fun init() {
        if (isInit) return
        with(binding) {
            viewModel.setMenuSortType(MenuSortType.RECENT_ADD)
            val navController = findNavController()
            val currentDest = navController.currentDestination?.id
            navController.addOnDestinationChangedListener { _, n, _ ->
                if (n.id != currentDest) {
                    if (isAdded && !isDetached) mainCategoryViewModel.savedTabPosition =
                        binding.mainCategoryTabLayout.selectedTabPosition
                }
            }
            basketListViewModel.observe(this@ShoppingListFragment) {
                var totalCount = 0
                it.forEach { shop ->
                    totalCount += shop.menuList.size
                }
                shoppingToolbarInclude.basketCountTextView.text = "$totalCount"
                if (shoppingToolbarInclude.basketGroup.isVisible) shoppingToolbarInclude.basketCountTextView.isVisible =
                    totalCount != 0
            }

            shoppingToolbarInclude.goBasketButtonSpace.setOnClickListener {
                view?.navigate(
                    R.id.basket_graph,
                    BasketMainFragmentArgs(
                        serviceTypeId = appViewModel.basketServiceType.id,
                        deliveryTypeId = appViewModel.basketDeliveryType.id
                    ).toBundle()
                )
            }

            shoppingToolbarInclude.goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }
            shoppingToolbarInclude.goSearchHeaderButton.setOnClickListener {
                goSearchFragment()
            }
            orderListHitBox.setOnClickListener {
                if (!loginInfoViewModel.isLoginState()) {
                    suggestLogin()
                    return@setOnClickListener
                }
                view?.navigate(
                    R.id.order_list_graph,
                    OrderListMainFragmentArgs(
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id
                    ).toBundle()
                )
            }
            searchView.setOnClickListener {
                it.navigate(R.id.search_main)
            }
            goSearchButton.setOnClickListener {
                goSearchFragment()
            }
            bannerViewAll.setOnClickListener {
                it.navigate(R.id.promotionListFragment)
            }
            homeHitBox.setOnClickListener {
                findNavController().fixedPopBackStack(R.id.mainFragment)
            }
            likedHitBox.setOnClickListener {
                if (!loginInfoViewModel.isLoginState()) {
                    suggestLogin()
                    return@setOnClickListener
                }
                view?.navigate(
                    R.id.like_list_graph, LikeListFragmentArgs(
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id
                    ).toBundle()
                )
            }
            myInfoHitBox.setOnClickListener {
                if (!loginInfoViewModel.isLoginState()) {
                    suggestLogin()
                    return@setOnClickListener
                }
                it.navigate(R.id.myinfo)
            }
            bannerViewModel =
                ViewModelProvider(this@ShoppingListFragment)[BannerViewModel::class.java]
            initViewPager2()
            initEventBanner()
        }
        isInit = true
    }

    private fun initEventBanner() {
        with(binding) {
            safeApiRequest(
                promotionAPI.getPromotionList(
                    promotionType = com.busandbt.code.PromotionType.BANNER.id,
                    promotionStatus = com.busandbt.code.PromotionStatus.PROGRESS.id,
                    displayLocation = PromotionScreenType.PARCEL_PRODUCT_LIST.id
                )
            ) {
                bannerViewPager.apply {
                    clipToOutline = true
                    bannerViewPagerAdapter = BannerViewPagerAdapter().apply {
                        setOnItemClick { view, _, item ->
                            view.navigate(
                                R.id.promotionDetailFragment,
                                PromotionDetailFragmentArgs(item.id).toBundle()
                            )
                        }
                    }
                    adapter = bannerViewPagerAdapter
                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            isRunning = true
                            pageNumber.text = "${position + 1}"
                            //직접 유저가 스크롤했을 떄!
                            bannerViewModel.setCurrentPosition(position)
                        }
                    })
                }
                bannerViewModel.bannerItemList.observe(viewLifecycleOwner) { bannerItemList ->
                    bannerViewPagerAdapter.setItems(bannerItemList)
                }
                bannerViewModel.currentPosition.observe(viewLifecycleOwner) { currentPosition ->
                    bannerViewPager.currentItem = currentPosition
                }
                pageAll.text = "/ ${it.size}"
                bannerViewModel.setBannerItems(it)
                registerTimerJob()
            }
        }
    }

    /**
     * 앱바 열려있는지 닫혀있는지 확인 후 시스템바 색 변경
     */
    private fun eventRegistrationAppBarChange() {
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val isCollapsed = kotlin.math.abs(verticalOffset) == appBarLayout.totalScrollRange
            val window: Window = requireActivity().window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            WindowInsetsControllerCompat(window, requireView()).isAppearanceLightStatusBars = true
            if (!isCollapsed) window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.shoppingSub)
            else window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        }
    }

    private fun initViewPager2() {
        with(binding) {
            val categoryList = categoryViewModel.mallCategoryList.toMutableList()
            categoryList.add(
                0,
                CategoryListItem(
                    0,
                    0,
                    ServiceType.SHOPPING_MALL.id,
                    "전체",
                    CategoryType.LARGE.id,
                    "",
                    emptyList()
                )
            )
            mainCategoryViewPager.adapter =
                MallMainCategoryTabAdapter(
                    this@ShoppingListFragment,
                    categoryList
                )
            mainCategoryTabLayout.eventRegistrationTabSelectedDifferenceFont(
                R.style.shoppingMainTabUnselectedTextAppearance,
                R.style.shoppingMainTabSelectedTextAppearance
            )
            //탭 텍스트랑 아이콘을 아이템 레이아웃을 하나 만들어 넣어준다
            TabLayoutMediator(mainCategoryTabLayout, mainCategoryViewPager) { tab, position ->
                val category = categoryList[position]
                val tabItemLayout =
                    layoutInflater.inflate(R.layout.item_mark_direct_delivery_tab, null).apply {
                        findViewById<TextView>(R.id.markText).text =
                            category.name
                    }
                tab.tag = category
                tab.customView = tabItemLayout
                //탭 아이템 사이 간격 주기
                for (i in 0 until mainCategoryTabLayout.tabCount) {
                    val t = (mainCategoryTabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                    val p = t.layoutParams as ViewGroup.MarginLayoutParams
                    p.setMargins(0, 0, SizeUtils.dp2px(-20F), 0)
                    t.requestLayout()
                }
            }.attach()
            mainCategoryTabLayout.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    debugLog("메인 탭 포지션", tab.position)
                    EventBus.getDefault().post(DoInitMallMenuListPagerFragmentEvent(tab.position))
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}
            })
            mainCategoryViewPager.offscreenPageLimit = categoryList.size + 1
            val position = mainCategoryViewModel.savedTabPosition
            when {
                position != null -> {
                    mainCategoryViewPager.post {
                        if ((mainCategoryTabLayout.selectedTabPosition.takeIf { it >= 0 }
                                ?: 0) == position) EventBus.getDefault()
                            .post(DoInitMallMenuListPagerFragmentEvent(position)) else mainCategoryViewPager.setCurrentItem(
                            position,
                            false
                        )
                    }
                }

                args.categoryId > 0 -> {
                    val c = categoryList.find { it.id == args.categoryId }
                    val index = categoryList.indexOfFirst { it.id == c?.id }
                    if (index != -1) {
                        mainCategoryViewPager.post {
                            if (mainCategoryTabLayout.selectedTabPosition == index) EventBus.getDefault()
                                .post(DoInitMallMenuListPagerFragmentEvent(index)) else mainCategoryViewPager.setCurrentItem(
                                index,
                                false
                            )
                        }
                    } else {
                    }
                }

                else -> {
                    mainCategoryViewPager.post {
                        EventBus.getDefault()
                            .post(DoInitMallMenuListPagerFragmentEvent(0))
                    }
                }
            }
        }
    }

    /**
     * 시간마다 동작해야하는 작업들을 정의한다.
     */
    private fun registerTimerJob() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    delay(3000)
                    bannerViewModel.getCurrentPosition()?.let {
                        bannerViewModel.setCurrentPosition((it.plus(1)) % 3)
                    }
                }
            }
        }
    }

    /**
     * 상점 상세 화면 내 검색화면으로 이동한다.
     */
    private fun goSearchFragment() {
        view?.navigate(
            R.id.search_shop_detail_graph,
            SearchShopDetailMainFragmentArgs(
                0,
                "",
                0,
                ServiceType.SHOPPING_MALL.id,
                DeliveryType.PARCEL.id
            ).toBundle()
        )
    }

    override fun onPause() {
        super.onPause()
        val window: Window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        isRunning = false
//        viewModel.setTabPosition(binding.mainCategoryTabLayout.selectedTabPosition)
    }

    override fun onResume() {
        super.onResume()
        eventRegistrationAppBarChange()
        isRunning = true
        appViewModel.basketServiceType = ServiceType.SHOPPING_MALL
        appViewModel.basketDeliveryType = DeliveryType.PARCEL
    }
}