package com.theone.busandbt.fragment.menu

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.MenuStatus
import com.google.android.material.appbar.AppBarLayout
import com.theone.busandbt.R
import com.theone.busandbt.adapter.ImageDetailPagerAdapter
import com.theone.busandbt.adapter.menu.MallMenuInfoListAdapter
import com.theone.busandbt.adapter.menu.MenuDetailImageListAdapter
import com.theone.busandbt.adapter.menu.MenuOptionGroupAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EnabledGoBasketButton
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.databinding.FragmentMallMenuDetailBinding
import com.theone.busandbt.dialog.CouponDownloadDialog
import com.theone.busandbt.dialog.ImageDetailDialog
import com.theone.busandbt.dialog.MallMenuBasketAddDialog
import com.theone.busandbt.dto.cost.MenuCostListItem
import com.theone.busandbt.dto.menu.CommonMenuDetail
import com.theone.busandbt.dto.menu.MallMenuDetail
import com.theone.busandbt.eventbus.RefreshMenuOptionEvent
import com.theone.busandbt.eventbus.menu.SmallRecyclerLoadEvent
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.basket.BasketMainFragmentArgs
import com.theone.busandbt.fragment.review.MenuReviewListFragmentArgs
import com.theone.busandbt.fragment.shop.ShoppingDetailFragmentArgs
import com.theone.busandbt.listener.AppBarStateChangeListener
import com.theone.busandbt.utils.MAX_BASKET_COUNT
import com.theone.busandbt.utils.MAX_MENU_COUNT
import com.theone.busandbt.utils.MIN_MENU_COUNT
import com.theone.busandbt.utils.ViewChangeUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import kotlin.properties.Delegates

/**
 * 택배배송(쇼핑몰) 메뉴상세 프래그먼트
 */
class MallMenuDetailsFragment : DataBindingFragment<FragmentMallMenuDetailBinding>(), EnabledGoBackButton,
    EnabledGoBasketButton,
    EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_mall_menu_detail
    private val args by navArgs<MallMenuDetailsFragmentArgs>()
    private val menuAPI: MenuAPI by inject()
    private lateinit var mallMenuDetail: MallMenuDetail
    private var commonMenuDetail: CommonMenuDetail? = null
    private var inputMinOrderCost = 0
    private var inputMaxDiscountCost = 0
    private var inputMallImageUrlList = emptyList<String>()
    private var inputDiscountRate = 0
    private var recyclerViewHeight = 0
    private val limitHeight = SizeUtils.dp2px(950F)
    private var selectedMenuCost: MenuCostListItem? = null

    private var count: Int by Delegates.observable(1) { _, _, newValue ->
        with(binding) {
            val cm = commonMenuDetail ?: return@with
            val adapter =
                menuOptionGroupRecyclerView.adapter as? MenuOptionGroupAdapter ?: return@observable
            val optionCost = adapter.items.flatMap {
                it.optionList.filter { option -> option.isSelected }
            }.sumOf { it.cost }
            countTextView.text = "$newValue"
            val totalMenuCost = ((cm.menuSaleCost + optionCost) * newValue).toMoneyFormat()
            addBtn.text = "${totalMenuCost}원 장바구니 담기"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputMinOrderCost = args.minOrderCost
        with(binding) {
            mainToolbar.setMarginsForSystemBars(requireContext())
            ViewChangeUtils.initExpandSystemBar(activity?.window)
            constraintLayout.setPadding(0, 0, 0, ViewChangeUtils.getNaviBarHeight(requireContext()))
            findNavController().addOnDestinationChangedListener { _, n, _ ->
                if (n.id != R.id.menuDetailsFragment) {
                    if (isAdded && !isDetached) ViewChangeUtils.initSystemBarReset(requireActivity())
                }
            }
            safeApiRequest(
                menuAPI.getMallMenuDetail(args.menuId)
            ) { md ->
                mallMenuDetail = md
                val smc = md.menuCostList.firstOrNull() ?: MenuCostListItem.createEmpty()
                selectedMenuCost = smc
                commonMenuDetail = CommonMenuDetail(
                    md.id,
                    md.name,
                    md.desc,
                    smc.cost,
                    smc.saleCost,
                    md.reviewCount,
                    md.imageUrlList,
                    md.optionGroupList,
                    md.remainCount,
                    MenuStatus.find(md.status),
                    md.available,
                    md.isAdultMenu
                )
                shoppingGuideTextInclude.paymentTypeTextView.text =
                    md.paymentTypeNameList.joinToString(",")
                shoppingGuideTextInclude.deliveryCostTextView.text = buildString {
                    append(md.deliveryCost.toCommonMoneyForm())
                    if (md.deliveryCost > 0 && md.orderCostForFree > 0) append("(${md.orderCostForFree.toCommonMoneyForm()} 이상 무료배송)")
                }

                inputMallImageUrlList = md.menuIntroImageUrlList
                inputMaxDiscountCost = md.maxDiscountCost
                inputDiscountRate = smc.discountRate
                whenSuccessLoadData()
                initMenuInfo()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ViewChangeUtils.initSystemBarReset(requireActivity())
    }

    private fun viewClickEvent() {
        with(binding) {
            // 더보기 버튼 클릭 이벤트 처리
            moreButtonView.setOnClickListener {
                downArrowImageView.isChecked = !downArrowImageView.isChecked
                if (moreButtonTextView.text == "상세정보 더보기") {
                    // 리사이클러뷰 높이 초기값으로 설정
                    val layoutParams = productImageRecyclerView.layoutParams
                    layoutParams.height = recyclerViewHeight
                    productImageRecyclerView.layoutParams = layoutParams
                    moreButtonTextView.text = "상세정보 접어보기"
                    gradientView.isVisible = false
                } else {
                    // 리사이클러뷰 높이 초기값으로 설정
                    val layoutParams = productImageRecyclerView.layoutParams
                    layoutParams.height = limitHeight
                    productImageRecyclerView.layoutParams = layoutParams
                    moreButtonTextView.text = "상세정보 더보기"
                    gradientView.isVisible = true
                }
            }

            couponDownloadInclude.root.setOnClickListener {
                val dialog = CouponDownloadDialog()
                dialog.arguments = bundleOf("shopId" to args.shopId)
                dialog.show(childFragmentManager, null)
            }

            with(refundInclude) {
                shopNameTextView.setOnClickListener {
                    downArrowImageView.isChecked = !downArrowImageView.isChecked
                }
                downArrowImageView.setOnCheckedChangeListener { _, isSelected ->
                    constraintLayout.isVisible = isSelected
                    shopNameTextView.isSelected =
                        !shopNameTextView.isSelected
                }
            }
        }
    }

    private fun initRecyclerViewAndSystemBar() {
        with(binding) {
            if (inputMallImageUrlList.isEmpty()) return
            productImageRecyclerView.adapter =
                MenuDetailImageListAdapter(inputMallImageUrlList)
            productImageRecyclerView.isVisible = true
        }
    }

    private fun whenSuccessLoadData() {
        val cm = commonMenuDetail ?: return
        with(binding) {
            viewClickEvent()
            footerForm.clipToOutline = true
            this.menuName = cm.menuName
            this.menuDesc = cm.menuDesc
            this.menuCost = cm.menuCost
            if (cm.isAdultMenu) {
                title.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, AppCompatResources.getDrawable(
                        root.context,
                        R.drawable.ic_adult_main
                    ), null
                )
            }
            discountTextInclude.menuCost = cm.menuCost
            discountTextInclude.menuSaleCost = cm.menuSaleCost
            discountTextInclude.discountRate = inputDiscountRate
            discountTextInclude.originalPriceTextView.paintFlags =
                discountTextInclude.originalPriceTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            this.reviewCount = cm.reviewCount
            this.maxDiscountCost = inputMaxDiscountCost
            val pagerAdapter =
                ImageDetailPagerAdapter(this@MallMenuDetailsFragment, cm.imageUrlList)
            menuImageViewPager.adapter = pagerAdapter
            dotsIndicator.attachTo(menuImageViewPager)

            if (cm.optionGroupList.isEmpty()) menuOptionGroupForm.isVisible = false
            menuOptionGroupRecyclerView.adapter = MenuOptionGroupAdapter(cm.optionGroupList)
            titleForm.setBackgroundResource(if (cm.imageUrlList.isNotEmpty()) R.drawable.bg_shop_details_layout else R.color.white)
            val totalMenuCost = (cm.menuSaleCost * count).toMoneyFormat()
            addBtn.text = "${totalMenuCost}원 장바구니 담기"

            basketListViewModel.observe(this@MallMenuDetailsFragment) {
                var totalCount = 0
                it.forEach { shop ->
                    totalCount += shop.menuList.size
                }
                mainToolbarContent.basketCountTextView.text = "$totalCount"
                if (mainToolbarContent.basketGroup.isVisible) mainToolbarContent.basketCountTextView.isVisible =
                    totalCount != 0
            }

            if (cm.imageUrlList.isNotEmpty()) {
                menuImageViewPager.setOnClickListener {
                    ImageDetailDialog().apply {
                        arguments = bundleOf(
                            "imageUrlList" to cm.imageUrlList.toTypedArray(),
                            "position" to menuImageViewPager.currentItem
                        )
                    }.show(childFragmentManager, null)
                }
                if (!cm.available) menuImageSoldOutLabel.isVisible = true
                menuImageForm.isVisible = true
            } else {
                mainToolbar.setBackgroundColor(Color.WHITE)
                menuImageForm.isVisible = false
                titleFormSpace.isVisible = true
                titleForm.setBackgroundResource(R.color.white)
            }

            initAppbar()

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
            reviewSee.setOnClickListener {
                if (reviewCount == 0) {
                    it.showMessageBar("아직 리뷰가 없습니다.")
                    return@setOnClickListener
                }
                it.navigate(
                    R.id.menu_review_list_graph,
                    MenuReviewListFragmentArgs(cm.menuId, cm.menuName).toBundle()
                )
            }
            plus.setOnClickListener {
                if (count >= MAX_MENU_COUNT) return@setOnClickListener
                count += 1
            }
            minus.setOnClickListener {
                if (count <= MIN_MENU_COUNT) return@setOnClickListener
                count -= 1
            }

            if (!cm.available) {
                addBtn.setBackgroundResource(R.drawable.bg_gray_long_btn)
                addBtn.isEnabled = false
            }

            // 장바구니에 추가
            addBtn.setOnClickListener {
                if (!cm.available) return@setOnClickListener
                tryAddBasket()
            }

            shopNameTextView.text = args.shopName
            goShopDetailButton.setOnClickListener {
                it.navigate(
                    R.id.shoppingdetails_graph,
                    ShoppingDetailFragmentArgs(
                        args.shopId,
                        args.serviceTypeId,
                        args.deliveryTypeId
                    ).toBundle()
                )
            }
            shopIntroGroup.isVisible = true
            menuOptionGroupForm.isVisible = false
            deliveryPriceGroup.isVisible = false
            discountTextInclude.root.isVisible = true
            menuDescTextView.isVisible = false
            space.isVisible = true
            footerGroup.isVisible = true
            refundInclude.root.isVisible = true
            productImageRecyclerView.isVisible = true
            shoppingGuideTextInclude.root.isVisible = true
            initRecyclerViewAndSystemBar()
            initReviewButton()
            root.post {
                root.playAlphaAnimation()
            }
        }
    }

    private fun initReviewButton() {
        with(binding) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(menuDetailForm) // 현재 ConstraintLayout
            constraintSet.clear(R.id.reviewSee, ConstraintSet.TOP)
            constraintSet.connect(
                R.id.reviewSee,
                ConstraintSet.TOP,
                R.id.discountTextInclude,
                ConstraintSet.BOTTOM
            )
            constraintSet.setMargin(R.id.reviewSee, ConstraintSet.TOP, SizeUtils.dp2px(28F))
            constraintSet.applyTo(menuDetailForm)
        }
    }

    private fun initAppbar() {
        val cm = commonMenuDetail ?: return
        with(binding) {
            appBar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                    with(mainToolbarContent) {
                        when (state) {
                            State.COLLAPSED -> {
                                if (cm.imageUrlList.isNotEmpty()) {
                                    goBackButton.setImageResource(R.drawable.ic_left_arrow)
                                    goBasketButton.setImageResource(R.drawable.ic_basket_black)
                                    titleForm.setBackgroundResource(R.color.white)
                                }
                                val t = titleTextView.text
                                if (t.isEmpty()) {
                                    if (cm.imageUrlList.isNotEmpty()) mainToolbarContent.root.setBackgroundResource(
                                        android.R.color.white
                                    )
                                    titleTextView.text = cm.menuName
                                }
                                systemBarColorView.isVisible = true
                            }

                            State.EXPANDED, State.IDLE -> {
                                if (cm.imageUrlList.isNotEmpty()) {
                                    goBackButton.setImageResource(R.drawable.ic_white_left_arrow)
                                    goBasketButton.setImageResource(R.drawable.ic_basket_white)
                                    titleForm.setBackgroundResource(R.drawable.bg_shop_details_layout)
                                }
                                val t = titleTextView.text
                                if (t.isNotEmpty()) {
                                    if (cm.imageUrlList.isNotEmpty()) mainToolbarContent.root.setBackgroundResource(
                                        android.R.color.transparent
                                    )
                                    titleTextView.text = ""
                                }
                                systemBarColorView.isVisible = false
                            }

                            else -> {}
                        }
                    }
                }
            })
        }
    }

    private fun tryAddBasket() {
        val da = deliveryAddressViewModel.selectedDeliveryAddress
        if (da == null) {
            view?.showMessageBar("주소 선택이 필요합니다.")
            return
        }
        if (basketListViewModel.getBasketMenuCount() >= MAX_BASKET_COUNT) {
            showMessageDialog(
                "장바구니가 가득 찼어요!\n상품을 비워주세요.",
                "상품은 최대 100개까지 담을 수 있습니다.",
                showCancelButton = true,
                showWarningImageView = true
            ) {
                onDoneButtonClick {
                    dismiss()
                }
            }
            return
        }
        with(MallMenuBasketAddDialog()) {
            val menuCount = binding.countTextView.text.toString().toIntOrNull() ?: return
            arguments = bundleOf("menuId" to args.menuId, "menuCount" to menuCount)
            show(this@MallMenuDetailsFragment.childFragmentManager, null)
        }
    }

    /**
     * 반품 및 교환 정보 초기화
     */
    private fun initRefundInfo() {
        with(binding) {

        }
    }

    /**
     * 상품정보제공고시 초기화
     */
    private fun initMenuInfo() {
        with(binding) {
            if (mallMenuDetail.menuInfoList.isNullOrEmpty()) return
            Log.d("ASdfsaf",mallMenuDetail.menuInfoList.toString())
            with(menuInfoInclude) {
                root.isVisible = true
                menuInfoList.adapter = MallMenuInfoListAdapter(mallMenuDetail.menuInfoList)
                shopNameTextView.setOnClickListener {
                    downArrowImageView.isChecked =
                        !downArrowImageView.isChecked
                }

                downArrowImageView.setOnCheckedChangeListener { _, isSelected ->
                    menuInfoList.isVisible = isSelected
                    shopNameTextView.isSelected =
                        !shopNameTextView.isSelected
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshMenuOption(event: RefreshMenuOptionEvent) {
        with(binding) {
            val md = commonMenuDetail ?: return
            val adapter =
                menuOptionGroupRecyclerView.adapter as? MenuOptionGroupAdapter ?: return
            val optionCost = adapter.items.flatMap {
                it.optionList.filter { option -> option.isSelected }
            }.sumOf { it.cost }
            val totalMenuCost = ((md.menuSaleCost + optionCost) * count).toMoneyFormat()
            addBtn.text = "${totalMenuCost}원 장바구니 담기"
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onItemsLoaded(event: SmallRecyclerLoadEvent) {
        binding.productImageRecyclerView.apply {
            post {
                val totalHeight = computeVerticalScrollRange()
                val shouldShowDetail = totalHeight > limitHeight

                layoutParams = layoutParams.apply {
                    height = if (shouldShowDetail) limitHeight else ViewGroup.LayoutParams.WRAP_CONTENT
                }
                isNestedScrollingEnabled = !shouldShowDetail
                binding.moreButtonGroup.isVisible = shouldShowDetail
            }
        }
    }
}