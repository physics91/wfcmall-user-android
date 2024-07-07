package com.theone.busandbt.fragment.shop

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.CategoryType
import com.busandbt.code.MenuSortType
import com.busandbt.code.ServiceType
import com.busandbt.code.UserStatus
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.ImageDetailPagerAdapter
import com.theone.busandbt.adapter.shop.MallMainCategoryTabAdapter
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentShoppingDetailBinding
import com.theone.busandbt.dialog.CouponDownloadDialog
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.dto.shop.MallShopDetail
import com.theone.busandbt.dto.shop.request.ShopLikeToggleRequest
import com.theone.busandbt.eventbus.menu.DoInitMallMenuMiddlePagerFragmentEvent
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.playAlphaAnimation
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.basket.BasketMainFragmentArgs
import com.theone.busandbt.fragment.review.ShopReviewListFragmentArgs
import com.theone.busandbt.fragment.search.SearchShopDetailMainFragmentArgs
import com.theone.busandbt.model.category.CategoryViewModel
import com.theone.busandbt.model.menu.MallMenuListViewModel
import com.theone.busandbt.model.menu.MallMenuMainCategoryViewModel
import com.theone.busandbt.model.shop.MallShopDetailViewModel
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * 쇼핑몰  상점상세
 */
class ShoppingDetailFragment : DataBindingFragment<FragmentShoppingDetailBinding>() {
    override val layoutId: Int = R.layout.fragment_shopping_detail
    private val args by navArgs<ShoppingDetailFragmentArgs>()
    private val shopAPI: ShopAPI by inject()
    private val viewModel: MallShopDetailViewModel by viewModels()
    private val mainCategoryViewModel: MallMenuMainCategoryViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModel()
    private val mallMenuListViewModel: MallMenuListViewModel by activityViewModels()
    private var isInit = false
    private val originalScrollListener =
        NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            with(binding) {
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

    override fun onPause() {
        super.onPause()
        mainCategoryViewModel.savedTabPosition = binding.mainCategoryTabLayout.selectedTabPosition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            initViewPager2()
            viewModel.shopDetailLiveData.observe(viewLifecycleOwner) {
                initView(it)
            }
            if (viewModel.shopDetailLiveData.value == null) {
                root.alpha = 0f
                safeApiRequest(
                    shopAPI.getMallShopDetail(args.shopId, memberId = loginInfo?.id)
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
            if (!isInit) {
                isInit = true
                mallMenuListViewModel.setMenuSortType(MenuSortType.RECENT_ADD)
            }
        }
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
                0,
                args.serviceTypeId,
                args.deliveryTypeId
            ).toBundle()
        )
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
                    this@ShoppingDetailFragment,
                    categoryList,
                    shopId = args.shopId,
                    viewType = 1
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
                    EventBus.getDefault().post(DoInitMallMenuMiddlePagerFragmentEvent(tab.position))
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}
            })
            mainCategoryViewPager.offscreenPageLimit = categoryList.size + 1
            val position = mainCategoryViewModel.savedTabPosition
            when {
                position != null -> {
                    mainCategoryViewPager.post {
                        if ((mainCategoryTabLayout.selectedTabPosition.takeIf { it >= 0 } ?: 0) == position) EventBus.getDefault()
                            .post(DoInitMallMenuMiddlePagerFragmentEvent(position)) else mainCategoryViewPager.setCurrentItem(
                            position,
                            false
                        )
                    }
                }

                else -> {
                    mainCategoryViewPager.post {
                        EventBus.getDefault()
                            .post(DoInitMallMenuMiddlePagerFragmentEvent(0))
                    }
                }
            }
        }
    }

    private fun initView(shopDetail: MallShopDetail) {
        with(binding) {
            this.shopDetail = shopDetail
            if (shopDetail.status != UserStatus.OPERATE.id) {
                offWorkNoticeTextView.isVisible = true
            }
            menuDetailGuideInclude.deliveryCostTextView.text = buildString {
                append(shopDetail.deliveryCost.toCommonMoneyForm())
                if (shopDetail.deliveryCost > 0 && shopDetail.orderCostForFree > 0) append("(${shopDetail.orderCostForFree.toCommonMoneyForm()} 이상 무료배송)")
            }
            menuDetailGuideInclude.paymentTypeTextView.text =
                shopDetail.paymentTypeNameList.joinToString(",")
            goSearchButton.text = "${shopDetail.name}에서 검색"
            basketListViewModel.observe(this@ShoppingDetailFragment) {
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

            initShopImageForm()
            initStarTextView()

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
                if (shopDetail.reviewCount == 0) {
                    it.showMessageBar("아직 리뷰가 없습니다.")
                    return@setOnClickListener
                }
                it.navigate(
                    R.id.shop_review_list,
                    ShopReviewListFragmentArgs(args.shopId).toBundle()
                )
            }
            shopInfoButton.setOnClickListener {
                it.navigate(
                    R.id.shop_info_graph,
                    ShopInfoFragmentArgs(args.shopId, args.serviceTypeId).toBundle()
                )
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
                    )
                ) {
                    likeCountTextView.text = if (likeButton.isSelected) {
                        loginInfo.likeCount -= 1
                        (likeCountTextView.text.toString()
                            .toInt() - 1).toString()
                    } else {
                        loginInfo.likeCount += 1
                        (likeCountTextView.text.toString()
                            .toInt() + 1).toString()
                    }
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
            if (sd.imageUrlList.isEmpty()) return
            val pagerAdapter =
                ImageDetailPagerAdapter(this@ShoppingDetailFragment, sd.imageUrlList)
            shopImageViewPager.adapter = pagerAdapter
            shopImageIndicator.attachTo(shopImageViewPager)
        }
    }
}