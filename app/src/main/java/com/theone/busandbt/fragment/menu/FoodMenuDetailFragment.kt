package com.theone.busandbt.fragment.menu

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.google.android.material.appbar.AppBarLayout
import com.theone.busandbt.R
import com.theone.busandbt.adapter.ImageDetailPagerAdapter
import com.theone.busandbt.adapter.cost.MenuCostListAdapter
import com.theone.busandbt.adapter.menu.MenuOptionGroupAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EnabledGoBasketButton
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.bindingadapter.ShopBindingAdapter
import com.theone.busandbt.databinding.FragmentFoodMenuDetailBinding
import com.theone.busandbt.dialog.ImageDetailDialog
import com.theone.busandbt.dto.cost.MenuCostListItem
import com.theone.busandbt.dto.menu.MenuDetail
import com.theone.busandbt.dto.menu.MenuOptionGroup
import com.theone.busandbt.eventbus.RefreshMenuOptionEvent
import com.theone.busandbt.eventbus.SelectMenuCostEvent
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.playAlphaAnimation
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.setMarginsForSystemBars
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.basket.BasketMainFragmentArgs
import com.theone.busandbt.fragment.review.MenuReviewListFragmentArgs
import com.theone.busandbt.listener.AppBarStateChangeListener
import com.theone.busandbt.utils.MAX_BASKET_COUNT
import com.theone.busandbt.utils.MAX_MENU_COUNT
import com.theone.busandbt.utils.MIN_MENU_COUNT
import com.theone.busandbt.utils.ViewChangeUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import kotlin.properties.Delegates

class FoodMenuDetailFragment : DataBindingFragment<FragmentFoodMenuDetailBinding>(),
    EnabledGoBackButton,
    EnabledGoBasketButton,
    EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_food_menu_detail
    private val args by navArgs<FoodMenuDetailFragmentArgs>()
    private val menuAPI: MenuAPI by inject()
    private var menuDetail: MenuDetail? = null
    private var menuCost: Int by Delegates.observable(0) { _, _, newValue ->
        with(binding) {
            val adapter =
                menuOptionGroupRecyclerView.adapter as? MenuOptionGroupAdapter ?: return@observable
            val optionCost = adapter.items.flatMap {
                it.optionList.filter { option -> option.isSelected }
            }.sumOf { it.cost }
            val totalMenuCost = ((newValue + optionCost) * count).toMoneyFormat()
            addBtn.text = "${totalMenuCost}원 장바구니 담기"
        }
    }

    private var count: Int by Delegates.observable(1) { _, _, newValue ->
        with(binding) {
            val adapter =
                menuOptionGroupRecyclerView.adapter as? MenuOptionGroupAdapter ?: return@observable
            val optionCost = adapter.items.flatMap {
                it.optionList.filter { option -> option.isSelected }
            }.sumOf { it.cost }
            countTextView.text = "$newValue"
            val totalMenuCost = ((menuCost + optionCost) * newValue).toMoneyFormat()
            addBtn.text = "${totalMenuCost}원 장바구니 담기"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            mainToolbar.setMarginsForSystemBars(requireContext())
            ViewChangeUtils.initExpandSystemBar(activity?.window)
            constraintLayout.setPadding(0, 0, 0, ViewChangeUtils.getNaviBarHeight(requireContext()))
            findNavController().addOnDestinationChangedListener { _, n, _ ->
                if (n.id != R.id.foodMenuDetailFragment) {
                    if (isAdded && !isDetached) ViewChangeUtils.initSystemBarReset(requireActivity())
                }
            }
            safeApiRequest(menuAPI.getMenuDetail(args.menuId)) { md ->
                menuDetail = md
                whenSuccessLoadData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ViewChangeUtils.initSystemBarReset(requireActivity())
    }

    private fun whenSuccessLoadData() {
        val md = menuDetail ?: return
        with(binding) {
            footerForm.clipToOutline = true
            this.menuName = md.name
            this.menuDesc = md.desc
            this.reviewCount = md.reviewCount
            if (md.isAdultMenu) {
                title.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, AppCompatResources.getDrawable(
                        root.context,
                        R.drawable.ic_adult_main
                    ), null
                )
            }
            if (args.deliveryTypeId != DeliveryType.PACKAGING.id) {
                this.minOrderCost = args.minOrderCost
                ShopBindingAdapter.minOrderCostTextForm(minOrderCostTextView, args.minOrderCost)
            }
            val pagerAdapter = ImageDetailPagerAdapter(this@FoodMenuDetailFragment, md.imageUrlList)
            menuImageViewPager.adapter = pagerAdapter
            dotsIndicator.attachTo(menuImageViewPager)

            if (md.optionGroupList.isEmpty()) menuOptionGroupForm.isVisible = false
            menuOptionGroupRecyclerView.adapter = MenuOptionGroupAdapter(md.optionGroupList)
            titleForm.setBackgroundResource(if (md.imageUrlList.isNotEmpty()) R.drawable.bg_shop_details_layout else R.color.white)
            val totalMenuCost = (menuCost * count).toMoneyFormat()
            addBtn.text = "${totalMenuCost}원 장바구니 담기"

            basketListViewModel.observe(this@FoodMenuDetailFragment) {
                var totalCount = 0
                it.forEach { shop ->
                    totalCount += shop.menuList.size
                }
                mainToolbarContent.basketCountTextView.text = "$totalCount"
                if (mainToolbarContent.basketGroup.isVisible) mainToolbarContent.basketCountTextView.isVisible =
                    totalCount != 0
            }

            if (md.imageUrlList.isNotEmpty()) {
                menuImageViewPager.setOnClickListener {
                    ImageDetailDialog().apply {
                        arguments =
                            bundleOf(
                                "imageUrlList" to md.imageUrlList.toTypedArray(),
                                "position" to menuImageViewPager.currentItem
                            )
                    }.show(childFragmentManager, null)
                }
                if (!md.available) menuImageSoldOutLabel.isVisible = true
                menuImageForm.isVisible = true
            } else {
                mainToolbar.setBackgroundColor(Color.WHITE)
                menuImageForm.isVisible = false
                titleFormSpace.isVisible = true
                titleForm.setBackgroundResource(R.color.white)
            }

            initAppbar()
            initMenuCostForm(md.menuCostList)

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
                    MenuReviewListFragmentArgs(md.id, md.name).toBundle()
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

            if (!md.available) {
                addBtn.setBackgroundResource(R.drawable.bg_gray_long_btn)
                addBtn.isEnabled = false
            }

            // 장바구니에 추가
            addBtn.setOnClickListener {
                if (!md.available) return@setOnClickListener
                tryAddBasket()
            }
            root.playAlphaAnimation()
        }
    }

    private fun initAppbar() {
        val md = menuDetail ?: return
        with(binding) {
            appBar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                    with(mainToolbarContent) {
                        when (state) {
                            State.COLLAPSED -> {
                                if (md.imageUrlList.isNotEmpty()) {
                                    goBackButton.setImageResource(R.drawable.ic_left_arrow)
                                    goBasketButton.setImageResource(R.drawable.ic_basket_black)
                                    titleForm.setBackgroundResource(R.color.white)
                                }
                                val t = titleTextView.text
                                if (t.isEmpty()) {
                                    if (md.imageUrlList.isNotEmpty()) mainToolbarContent.root.setBackgroundResource(
                                        android.R.color.white
                                    )
                                    titleTextView.text = md.name
                                }
                                systemBarColorView.isVisible = true
                            }

                            State.EXPANDED, State.IDLE -> {
                                if (md.imageUrlList.isNotEmpty()) {
                                    goBackButton.setImageResource(R.drawable.ic_white_left_arrow)
                                    goBasketButton.setImageResource(R.drawable.ic_basket_white)
                                    titleForm.setBackgroundResource(R.drawable.bg_shop_details_layout)
                                }
                                val t = titleTextView.text
                                if (t.isNotEmpty()) {
                                    if (md.imageUrlList.isNotEmpty()) mainToolbarContent.root.setBackgroundResource(
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

    /**
     * 메뉴 가격을 보여주는 방식만 결정한다.
     */
    private fun initMenuCostForm(menuCostList: List<MenuCostListItem>?) {
        with(binding) {
            menuCostList ?: return
            val firstMenuCost = menuCostList.firstOrNull()
            menuCost = firstMenuCost?.saleCost ?: 0

            val isAllUnNamed = menuCostList.find { it.name.isNotEmpty() } == null
            if (isAllUnNamed) {
                menuCostUnNamedInclude.root.isVisible = true
                menuCostSelectableInclude.root.isVisible = false
                CommonBindingAdapter.commonCost(
                    menuCostUnNamedInclude.totalMenuCostTextView,
                    menuCost
                )
            } else {
                menuCostUnNamedInclude.root.isVisible = false
                menuCostSelectableInclude.root.isVisible = true
                firstMenuCost?.isSelected = true
                menuCostSelectableInclude.menuCostRecyclerView.adapter =
                    MenuCostListAdapter(menuCostList.filter { it.name.isNotEmpty() })
            }
        }
    }

    private fun getSelectedMenuCost(): MenuCostListItem? {
        with(binding) {
            val md = menuDetail ?: return null
            val menuCostList = md.menuCostList ?: emptyList()
            val isAllUnNamed = menuCostList.find { it.name.isNotEmpty() } == null
            if (isAllUnNamed) return menuCostList.firstOrNull()
            else {
                val adapter =
                    menuCostSelectableInclude.menuCostRecyclerView.adapter as? MenuCostListAdapter
                        ?: return null
                return adapter.items.find { it.isSelected }
            }
        }
    }

    /**
     * 장바구니에 메뉴를 넣는다.
     * 경우에 따라선 장바구니에 안들어갈 수 있다.
     */
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
        val md = menuDetail ?: return
        val adapter =
            binding.menuOptionGroupRecyclerView.adapter as? MenuOptionGroupAdapter ?: return
        val optionGroupList = ArrayList<MenuOptionGroup>()
        adapter.items.forEach { optionGroup ->
            optionGroupList.add(optionGroup.copy(optionList = optionGroup.optionList.filter { option -> option.isSelected }))
        }
        debugLog("옵션 그룹 추가", optionGroupList)
        val mc = getSelectedMenuCost()
        if (mc == null) {
            view?.showMessageBar("잘못된 접근입니다.")
            findNavController().popBackStack()
            return
        }
        basketListViewModel.add(
            args.shopId,
            args.shopName,
            args.serviceTypeId,
            args.deliveryTypeId,
            0,
            0,
            md.id,
            md.status,
            md.name,
            md.isAdultMenu,
            menuCost,
            menuCost,
            count,
            md.imageUrlList.firstOrNull() ?: "",
            optionGroupList,
            mc.id,
            mc.name
        )
        view?.showMessageBar("장바구니에 메뉴가 추가되었습니다.")
        findNavController().popBackStack()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshMenuOption(event: RefreshMenuOptionEvent) {
        with(binding) {
            val adapter =
                menuOptionGroupRecyclerView.adapter as? MenuOptionGroupAdapter ?: return
            val optionCost = adapter.items.flatMap {
                it.optionList.filter { option -> option.isSelected }
            }.sumOf { it.cost }
            val totalMenuCost = ((menuCost + optionCost) * count).toMoneyFormat()
            addBtn.text = "${totalMenuCost}원 장바구니 담기"
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSelectMenuCost(event: SelectMenuCostEvent) {
        menuCost = event.selected.saleCost
    }
}