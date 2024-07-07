package com.theone.busandbt.fragment.shop

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.busandbt.code.UserStatus
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.ImageDetailPagerAdapter
import com.theone.busandbt.adapter.menu.MenuGroupAdapter
import com.theone.busandbt.adapter.menu.ReptMenuListAdapter
import com.theone.busandbt.adapter.shop.ShopDetailsTabAdapter
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentShopDetailsBinding
import com.theone.busandbt.dialog.CouponDownloadDialog
import com.theone.busandbt.dto.menu.Menu
import com.theone.busandbt.dto.shop.ShopDetail
import com.theone.busandbt.dto.shop.request.ShopLikeToggleRequest
import com.theone.busandbt.eventbus.SelectMenuEvent
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.basket.BasketMainFragmentArgs
import com.theone.busandbt.fragment.menu.FoodMenuDetailFragmentArgs
import com.theone.busandbt.fragment.search.SearchShopDetailMainFragmentArgs
import com.theone.busandbt.function.getOrderDoneMinutesText
import com.theone.busandbt.model.shop.ShopDetailViewModel
import com.theone.busandbt.spanned.TypefaceSpanCompat
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject

/**
 * 상점 상세 화면
 * 상점에 대한 간략한 정보와 메뉴 정보를 볼 수 있음
 * 대표메뉴가 있을때와 없을때 처리가 다름
 */
class ShopDetailFragment : DataBindingFragment<FragmentShopDetailsBinding>(), EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_shop_details
    private val args by navArgs<ShopDetailFragmentArgs>()
    private val shopAPI: ShopAPI by inject()
    private val viewModel: ShopDetailViewModel by viewModels()
    private var canScrollToMoveTab = true
    private val originalTabSelectListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            with(binding) {
                if (tab == null) return
                val tabPosition = tab.position
                viewModel.menuGroupTabPosition = tabPosition
                val target = if (hasReptMenu) {
                    if (tabPosition == 0) reptMenuListRecyclerView else menuRecyclerView[tabPosition - 1]
                } else {
                    menuRecyclerView[tabPosition]
                }
                canScrollToMoveTab = false
                mainScrollView.smoothScrollToView(target, maxDuration = 1500) {
                    canScrollToMoveTab = true
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
    }
    private val originalScrollListener =
        NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            with(binding) {
                if (canScrollToMoveTab) {
                    val reptBottom =
                        if (hasReptMenu) reptMenuListRecyclerView.bottom else 0
                    if (hasReptMenu) {
                        if (scrollY in 0..reptBottom) {
                            if (menuTab.selectedTabPosition != 0) menuTab.selectTabWithNoSelectEffect(
                                0
                            )
                        } else {
                            menuRecyclerView.scrollToSelectedTabWithFactor(
                                scrollY,
                                true,
                                reptBottom
                            )
                        }
                    } else {
                        menuRecyclerView.scrollToSelectedTabWithFactor(scrollY, false, reptBottom)
                    }
                }
                val sd = shopDetail ?: return@with
                if (scrollY >= shopNameTextView.bottom) { // 스크롤이 상점명 밑까지 내려왔을때
                    if (sd.imageUrlList.isNotEmpty()) {
                        mainToolbarContent.goBackButton.setImageResource(R.drawable.ic_left_arrow)
                        mainToolbarContent.goBasketButton.setImageResource(R.drawable.ic_basket_black)
                        mainToolbarContent.goSearchHeaderButton.setImageResource(R.drawable.ic_search_black)
                    }
                    val t = mainToolbarContent.titleTextView.text
                    if (t.isEmpty()) {
                        if (sd.imageUrlList.isNotEmpty()) {
                            mainToolbarForm.setBackgroundResource(R.color.white)
                        }
                        mainToolbarContent.titleTextView.text = sd.name
                    }
                } else {
                    if (sd.imageUrlList.isNotEmpty()) {
                        mainToolbarContent.goBackButton.setImageResource(R.drawable.ic_white_left_arrow)
                        mainToolbarContent.goBasketButton.setImageResource(R.drawable.ic_basket_white)
                        mainToolbarContent.goSearchHeaderButton.setImageResource(R.drawable.ic_search_white)
                    }
                    val t = mainToolbarContent.titleTextView.text
                    if (t.isNotEmpty()) {
                        if (sd.imageUrlList.isNotEmpty()) {
                            mainToolbarForm.setBackgroundResource(
                                android.R.color.transparent
                            )
                        }
                        mainToolbarContent.titleTextView.text = ""
                    }
                }
            }
        }
    private var hasReptMenu: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            viewModel.shopDetailLiveData.observe(viewLifecycleOwner) {
                if (it == null) return@observe
                initView(it)
            }
            if (viewModel.shopDetailLiveData.value == null) {
                root.alpha = 0f
                safeApiRequest(
                    shopAPI.getShopDetail(args.shopId, memberId = loginInfo?.id)
                ) {
                    viewModel.setShopDetail(it)
                }
            }
            mainToolbarContent.goSearchHeaderButton.setOnClickListener {
                goSearchFragment()
            }
            goSearchButton.setOnClickListener {
                goSearchFragment()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.deliveryTypeTabPosition = binding.deliveryTab.selectedTabPosition
    }

    /**
     * 상점 상세 화면 내 검색화면으로 이동한다.
     */
    private fun goSearchFragment() {
        val sd = binding.shopDetail ?: return
        view?.navigate(
            R.id.search_shop_detail_graph,
            SearchShopDetailMainFragmentArgs(
                args.shopId,
                sd.name,
                sd.minOrderCost,
                args.serviceTypeId,
                args.deliveryTypeId
            ).toBundle()
        )
    }

    private fun RecyclerView.scrollToSelectedTabWithFactor(
        targetYPosition: Int,
        hasReptMenu: Boolean,
        baseBottom: Int
    ) {
        with(binding) {
            val factor = if (hasReptMenu) 1 else 0
            children.forEachIndexed { i, view ->
                val y = view.top + baseBottom
                val maxY = y + view.measuredHeight
                if (targetYPosition in y..maxY && menuTab.selectedTabPosition != i + factor) {
                    menuTab.selectTabWithNoSelectEffect(i + factor)
                    return@forEachIndexed
                }
            }
        }
    }

    private fun initView(shopDetail: ShopDetail) {
        with(binding) {
            this.shopDetail = shopDetail
            if (shopDetail.status != UserStatus.OPERATE) {
                offWorkNoticeTextView.isVisible = true
            }
            goSearchButton.text = "${shopDetail.name}에서 검색"
            basketListViewModel.observe(this@ShopDetailFragment) {
                var totalCount = 0
                it.forEach { shop ->
                    totalCount += shop.menuList.size
                }
                mainToolbarContent.basketCountTextView.text = "$totalCount"
                if (mainToolbarContent.basketGroup.isVisible) mainToolbarContent.basketCountTextView.isVisible =
                    totalCount != 0
            }
            likeButton.isSelected = shopDetail.liked
            if (shopDetail.imageUrlList.isEmpty()) {
                mainToolbarForm.setBackgroundColor(Color.WHITE)
            }

            initDeliveryTypeTab(
                shopDetail.deliveryTypeList
                    .filter { d -> d in arrayOf(1, 2, 3) } // 현재 바로배달, 포장주문, 묶음배달만 지원
                    .map { deliveryTypeId ->
                        DeliveryType.find(
                            deliveryTypeId
                        )
                    })

            initMenu()
            initShopImageForm()
            initStarTextView()
            tabMove()

            mainToolbarContent.goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }

            mainToolbarContent.goBasketButtonSpace.setOnClickListener {
                val navController = findNavController()
                val dest = navController.currentDestination ?: return@setOnClickListener
                navController.navigate(
                    R.id.basket_graph,
                    BasketMainFragmentArgs(
                        appViewModel.basketServiceType.id,
                        appViewModel.basketDeliveryType.id
                    ).toBundle(),
                    NavOptions.Builder().setPopUpTo(dest.id, false).build()
                )
            }

            doShareSpace.setOnClickListener {
                it.showMessageBar("준비중인 기능입니다.")
            }
            goReviewListSpace.setOnClickListener {
                val action =
                    ShopDetailFragmentDirections.actionShopDetailsFragmentToFullReviewFragment(
                        args.shopId
                    )
                findNavController().navigate(action)
            }
            shopInfoButton.setOnClickListener {
                val action =
                    ShopDetailFragmentDirections.actionShopDetailsFragmentToStoreInformationFragment(
                        args.shopId,
                        args.serviceTypeId
                    )
                findNavController().navigate(action)
            }
            toggleLikeSpace.setOnClickListener {
                val loginInfo = loginInfoViewModel.getLoginInfo()
                if (loginInfo == null) {
                    suggestLogin()
                    return@setOnClickListener
                }
                safeApiRequest(
                    shopAPI.toggleLike(
                        loginInfo.getFormedToken(),
                        args.shopId,
                        ShopLikeToggleRequest(loginInfo.id, !likeButton.isSelected)
                    ),
                    showProgress = false
                ) {
                    val factor = if (likeButton.isSelected) -1 else 1
                    loginInfo.likeCount += factor
                    val c = when {
                        !likeButton.isSelected -> if (!shopDetail.liked) 1 else 0
                        else -> if (shopDetail.liked) -1 else 0
                    }
                    likeCountTextView.text = "${shopDetail.likeCount + c}"
                    likeButton.isSelected = !likeButton.isSelected
                }
            }
            couponDownloadInclude.root.setOnClickListener {
                val dialog = CouponDownloadDialog()
                dialog.arguments = bundleOf("shopId" to args.shopId)
                dialog.show(childFragmentManager, null)
            }

            mainScrollView.setOnScrollChangeListener(originalScrollListener)
            root.playAlphaAnimation()
        }
    }

    private fun initMenu() {
        with(binding) {
            val sd = shopDetail ?: return
            if (sd.menuGroupList.isNotEmpty()) {
                val menuGroupAdapter = MenuGroupAdapter(sd.menuGroupList)
                menuRecyclerView.adapter = menuGroupAdapter
                menuRecyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(24F)))
            } else { // 메뉴가 없을 경우
                menuRecyclerViewForm.isVisible = false
            }
            val reptMenuList: List<Menu> =
                sd.menuGroupList.flatMap { it.menuList.filter { menu -> menu.isMainMenu } }
            initReptMenu(reptMenuList) // 대표메뉴 UI 설정
            if (reptMenuList.isNotEmpty()) {
                hasReptMenu = true
                menuTab.addTab(menuTab.newTab().setText("대표메뉴"))
            }
            for (tab in sd.menuGroupList) {
                menuTab.addTab(menuTab.newTab().setText(tab.name))
            }
            menuTab.addOnTabSelectedListener(originalTabSelectListener)
        }
    }

    private fun TabLayout.selectTabWithNoSelectEffect(tabPosition: Int) {
        val tab = getTabAt(tabPosition)
        removeOnTabSelectedListener(originalTabSelectListener)
        selectTab(tab)
        addOnTabSelectedListener(originalTabSelectListener)
    }

    //선택에 따라 탭에 맞게 화면전환
    private fun tabMove() {
        with(binding) {
            val menuGroupSavedPosition = viewModel.menuGroupTabPosition
            if (menuGroupSavedPosition != null) {
                menuTab.post {
                    menuTab.selectTab(menuTab.getTabAt(menuGroupSavedPosition))
                }
            }

            debugLog("으잉?", "ㅇ")

            val savedPosition = viewModel.deliveryTypeTabPosition
            if (savedPosition != null) {
                deliveryViewPager.doOnPreDraw {
                    deliveryViewPager.setCurrentItem(savedPosition, false)
                }
                return
            }

            deliveryTab.post {
                var position = 0
                (0 until deliveryTab.tabCount).forEach {
                    val tab = deliveryTab.getTabAt(it) ?: return@forEach
                    if (tab.tag == args.deliveryTypeId) {
                        debugLog("포지션 당첨", it)
                        position = it
                    }
                }
                debugLog("포지션", position)
                deliveryViewPager.doOnPreDraw {
                    deliveryViewPager.setCurrentItem(position, false)
                }
            }
        }
    }

    private fun initStarTextView() {
        val sd = binding.shopDetail ?: return
        with(binding.starTextView) {
            text = buildSpannedString {
                append(
                    sd.star.toString(),
                    StyleSpan(Typeface.BOLD),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("(${if (sd.reviewCount > 999) "999+" else sd.reviewCount})")
            }
        }
    }

    private fun initShopImageForm() {
        with(binding) {
            val sd = shopDetail ?: return
            val flag = sd.imageUrlList.isEmpty()
            shopDetailHeaderFormSpace.setBackgroundResource(if (flag) R.color.white else R.drawable.bg_detail_header)
            if (flag) {
                val lp = shopNameTextView.layoutParams as? MarginLayoutParams ?: return
                lp.topMargin = SizeUtils.dp2px(80f)
                return
            }
            mainToolbarContent.goBackButton.setImageResource(R.drawable.ic_white_left_arrow)
            mainToolbarContent.goBasketButton.setImageResource(R.drawable.ic_basket_white)
            mainToolbarContent.goSearchHeaderButton.setImageResource(R.drawable.ic_search_white)
            val pagerAdapter = ImageDetailPagerAdapter(this@ShopDetailFragment, sd.imageUrlList)
            shopImageViewPager.adapter = pagerAdapter
            shopImageIndicator.attachTo(shopImageViewPager)
        }
    }

    private fun initReptMenu(reptMenuList: List<Menu>) {
        with(binding) {
            if (reptMenuList.isEmpty()) {
                reptMenuGroup.isVisible = false
                reptMenuBottomSpace.isVisible = false
                return
            }
            val sd = shopDetail ?: return
            reptMenuListRecyclerView.adapter =
                ReptMenuListAdapter(reptMenuList).apply {
                    setOnItemClick { _, _, item ->
                        val tab = deliveryTab.getTabAt(deliveryTab.selectedTabPosition)
                        val deliveryTypeId = tab?.tag as? Int ?: return@setOnItemClick
                        view?.navigate(
                            R.id.food_menu_detail_graph,
                            FoodMenuDetailFragmentArgs(
                                item.id,
                                args.shopId,
                                sd.name,
                                ServiceType.FOOD_DELIVERY.id,
                                deliveryTypeId,
                                sd.minOrderCost
                            ).toBundle()
                        )
                    }
                }
        }
    }

    /**
     * 배달유형 탭 초기화
     */
    @SuppressLint("SetTextI18n")
    private fun initDeliveryTypeTab(deliveryTypeList: List<DeliveryType>) {
        if (deliveryTypeList.isEmpty()) return
//        safe {
        with(binding) {
            val sd = shopDetail ?: return@with
            deliveryViewPager.adapter =
                ShopDetailsTabAdapter(
                    childFragmentManager,
                    lifecycle,
                    args.shopId,
                    sd,
                    deliveryTypeList,
                    args.serviceTypeId
                )
            // 배달유형이 없거나 하나뿐일 경우 탭을 가린다.
            if (deliveryTypeList.isEmpty() || deliveryTypeList.size == 1) {
                deliveryTab.isVisible = false
                (deliveryTabUnderLine.layoutParams as LayoutParams).setMargins(
                    0,
                    SizeUtils.dp2px(31f),
                    0,
                    0
                )
            }
            deliveryTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
                
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab ?: return
                    val deliveryTypeId = tab.tag as? Int ?: return
                    appViewModel.basketDeliveryType = DeliveryType.find(deliveryTypeId)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }
            })
            TabLayoutMediator(deliveryTab, deliveryViewPager) { tab, position ->
                val deliveryType = deliveryTypeList[position]
                tab.tag = deliveryType.id
                val tabItemLayout =
                    layoutInflater.inflate(R.layout.item_delivery_type_tab, null).apply {
                        findViewById<ImageView>(R.id.tab_icon_imageView).setImageResource(
                            when (deliveryType) {
                                DeliveryType.INSTANT -> R.drawable.ic_instant_delivery
                                DeliveryType.PACKAGING, DeliveryType.BUNDLE -> R.drawable.ic_packaging
                                else -> R.drawable.ic_instant_delivery
                            }
                        )
                        val orderDoneMinutes = sd.getOrderDoneMinutes(deliveryType)
                        val tabTitleTextView = findViewById<TextView>(R.id.tab_title_imageView)
                        tabTitleTextView.text = buildSpannedString {
                            append(
                                deliveryType.desc(),
                                TypefaceSpanCompat(
                                    ResourcesCompat.getFont(
                                        requireContext(),
                                        R.font.sult_bold
                                    ) ?: return@buildSpannedString
                                ),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            append("\n${getOrderDoneMinutesText(orderDoneMinutes, true)}")
                        }
                    }
                tab.customView = tabItemLayout
            }.attach()
//            }
        }
    }

    /**
     * 메뉴 상세 화면으로 이동시킨다.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSelectMenu(event: SelectMenuEvent) {
        with(binding) {
            val sd = binding.shopDetail ?: return
            val tab = deliveryTab.getTabAt(deliveryTab.selectedTabPosition)
            val deliveryTypeId = tab?.tag as? Int ?: return@with
            view?.navigate(
                R.id.food_menu_detail_graph,
                FoodMenuDetailFragmentArgs(
                    event.menuId,
                    args.shopId,
                    sd.name,
                    ServiceType.FOOD_DELIVERY.id,
                    deliveryTypeId,
                    sd.minOrderCost
                ).toBundle()
            )
        }
    }
}