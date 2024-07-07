package com.theone.busandbt.fragment.basket

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.basket.BasketViewPagerAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentBasketMainBinding
import com.theone.busandbt.eventbus.basket.ChangeBasketMenuEvent
import com.theone.busandbt.eventbus.basket.ChangeBasketTabEvent
import com.theone.busandbt.eventbus.basket.ChangeSelectedBasketShopEvent
import com.theone.busandbt.eventbus.basket.RemoveBasketMenuEvent
import com.theone.busandbt.extension.calculateMenuCost
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.findDeliveryCost
import com.theone.busandbt.extension.playAlphaAnimation
import com.theone.busandbt.extension.querySetParam
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.setTextAppearanceAtChild
import com.theone.busandbt.extension.setTextAppearanceCompat
import com.theone.busandbt.extension.setTextAtChild
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.item.basket.BasketShop
import com.theone.busandbt.model.basket.BasketMainViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject

/**
 * 장바구니 메인화면
 * TODO - 현재 선택된 주소와 배달 상점의 주소를 비교해야함  , 쇼핑몰 탭 일 때 하단 버튼이 안 보임  쇼핑몰 장바구니 기능은 전체적으로 수정 필요함!
 */
class BasketMainFragment : BaseBasketFragment<FragmentBasketMainBinding>(),
    RequiredDeliveryAddress, EnabledGoBackButton, EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_basket_main
    override val actionBarTitle: String = "장바구니"
    private val args by navArgs<BasketMainFragmentArgs>()
    private val shopAPI: ShopAPI by inject()
    private val viewModel: BasketMainViewModel by viewModels()

    /**
     * 아래 두가지 유형으로 현재 선택된 탭들을 판단한다.
     */
    private var tabServiceType: ServiceType? = null
    private var tabDeliveryType: DeliveryType? = null

    override fun onStop() {
        super.onStop()
        basketListViewModel.save()
    }

    override fun onPause() {
        super.onPause()
        with(binding) {
            viewModel.setServiceTabPosition(basketTabLayout.selectedTabPosition)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
        initTabPosition()
        with(binding) {
            safeApiRequest(
                shopAPI.getBasketInfo(
                    da.jibun,
                    da.lat,
                    da.lng,
                    basketListViewModel.getBasketShopIdList().querySetParam,
                    basketListViewModel.getBasketMenuIdList().querySetParam,
                    basketListViewModel.getBasketOptionIdList().querySetParam
                )
            ) {
                basketListViewModel.updateBasketMenuList(
                    it.menuBasketInfoList,
                    it.optionBasketInfoList
                )
                basketListViewModel.basketInfoMap =
                    it.shopBasketInfoList.associateBy { basketInfo -> basketInfo.shopId }
                basketListViewModel.observe(viewLifecycleOwner) {
                    updateTab(ServiceType.FOOD_DELIVERY.id, DeliveryType.INSTANT.id)
                    updateTab(ServiceType.FOOD_DELIVERY.id, DeliveryType.PACKAGING.id)
                    updateTab(ServiceType.SHOPPING_MALL.id, DeliveryType.PARCEL.id)
                }
                basketListViewModel.observeAllSelectedBasketShop(viewLifecycleOwner) { tab, basketShop ->
                    val serviceType = tabServiceType ?: return@observeAllSelectedBasketShop
                    val deliveryType = tabDeliveryType ?: return@observeAllSelectedBasketShop
                    if (serviceType != tab.serviceType || deliveryType != tab.deliveryType) return@observeAllSelectedBasketShop
                    if (basketShop == null) {
                        footerForm.isVisible = false
                        return@observeAllSelectedBasketShop
                    }
                    refreshFooter(basketShop)
                }
                orderButton.setOnClickListener { // 주문하기 버튼
                    if (!loginInfoViewModel.isLoginState()) {
                        suggestLogin()
                        return@setOnClickListener
                    }
                    val serviceType = tabServiceType ?: return@setOnClickListener
                    val deliveryType = tabDeliveryType ?: return@setOnClickListener

                    val (title, subTitle) = when (deliveryType) {
                        DeliveryType.INSTANT -> "배달 주문이 맞나요?" to "메뉴가 준비되는 즉시 주소지로 배달됩니다."
                        DeliveryType.PACKAGING -> "포장 주문이 맞나요?" to "고객님께서 직접 매장에 방문하여\n픽업하는 주문입니다."
                        DeliveryType.PARCEL -> "택배배송 주문이 맞나요?" to "주문 시 일반 택배로 배송됩니다."
                        else -> return@setOnClickListener
                    }
                    showMessageDialog(
                        title,
                        subTitle,
                        showCancelButton = true
                    ) {
                        onDoneButtonClick {
                            val basketShop =
                                basketListViewModel.getSelectedBasketShop(
                                    serviceType,
                                    deliveryType
                                )
                                    ?: return@onDoneButtonClick
                            val basketInfoMap =
                                basketListViewModel.basketInfoMap ?: return@onDoneButtonClick
                            val basketInfo =
                                basketInfoMap[basketShop.shopId] ?: return@onDoneButtonClick
                            val menuCost = basketShop.menuList.calculateMenuCost()
                            val action =
                                BasketMainFragmentDirections.actionShopBasketMainFragmentToAddOrderFragment(
                                    serviceType.id,
                                    deliveryType.id,
                                    basketInfo.deliveryCostList.findDeliveryCost(menuCost) + basketInfo.extraCost
                                )
                            findNavController().navigate(action)
                            dismiss()
                        }
                    }
                }

                initView()
                root.playAlphaAnimation()
            }
        }
    }

    private fun initView() {
        with(binding) {
            initBasketTabLayout(basketTabLayout)
            basketViewPager.adapter =
                BasketViewPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(basketTabLayout, basketViewPager) { tab, position ->
                val (tabServiceType, tabDeliveryType) = when (position) {
                    0 -> ServiceType.FOOD_DELIVERY to DeliveryType.INSTANT
                    1 -> ServiceType.FOOD_DELIVERY to DeliveryType.PACKAGING
                    2 -> ServiceType.SHOPPING_MALL to DeliveryType.PARCEL
                    else -> return@TabLayoutMediator
                }
                val basketCount = calculateBasketCount(tabServiceType.id, tabDeliveryType.id)
                val tabTitle = buildTabTitle(tabServiceType.id, tabDeliveryType.id, basketCount)
                tab.text = tabTitle
            }.attach()
            initTabPosition()
        }
    }

    /**
     * 탭이 선택 됐을 때 addOnTabSelectedListener 이벤트에 등록하여
     * 선택 된 탭의 폰트와,백그라운드를 바꿔준다
     */
    private fun initBasketTabLayout(
        tabLayout: TabLayout,
    ) {
        val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val textView =
                    ((tabLayout.getChildAt(0) as LinearLayout).getChildAt(tab.position) as LinearLayout).getChildAt(
                        1
                    ) as TextView
                textView.setTextAppearanceCompat(R.style.tabSelectedTextAppearance)

                for (i in 0 until tabLayout.tabCount) {
                    val innerTab = tabLayout.getTabAt(i)
                    when (i) {
                        0 -> innerTab?.view?.setBackgroundResource(R.drawable.bg_tab_select_position_first)
                        tabLayout.tabCount - 1 -> innerTab?.view?.setBackgroundResource(R.drawable.bg_tab_select_position_last)
                        else -> innerTab?.view?.setBackgroundResource(R.drawable.bg_tab_select_position_others)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val textView =
                    ((tabLayout.getChildAt(0) as LinearLayout).getChildAt(tab.position) as LinearLayout).getChildAt(
                        1
                    ) as TextView
                textView.setTextAppearanceCompat(R.style.tabUnselectedTextAppearance)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        }

        tabLayout.addOnTabSelectedListener(tabSelectedListener)
        // 시작하자마자 현재 선택된 탭에 대한 처리
        val selectedTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)
        if (selectedTab != null) {
            tabSelectedListener.onTabSelected(selectedTab)
        }
    }

    private fun refreshFooter(basketShop: BasketShop) {
        with(binding) {
            val menuCost = basketShop.menuList.calculateMenuCost()
            if (menuCost == 0) { // 0원일땐 안보여야한다.
                footerForm.isVisible = false
                return@with
            }
            footerForm.isVisible = true
            val basketInfoMap = basketListViewModel.basketInfoMap ?: return@with
            val basketInfo = basketInfoMap[basketShop.shopId] ?: return@with
            val deliveryCost =
                if (tabDeliveryType != DeliveryType.PACKAGING) basketInfo.deliveryCostList.findDeliveryCost(
                    menuCost
                ) else 0
            val actualExtraCost =
                if (tabDeliveryType != DeliveryType.PACKAGING) basketInfo.extraCost else 0
            // TODO - 포장할인도 계산해야한다.
            val totalCost = menuCost + deliveryCost + actualExtraCost
            val costText = getString(
                R.string.commonCost,
                totalCost.toMoneyFormat()
            )
            orderButton.text = "총 $costText 주문하기"
            if (basketShop.deliveryTypeId != DeliveryType.PACKAGING.id) {
                contentTextView.text = "결제 시 쿠폰 적용이 가능하며 배달비가 추가될 수 있습니다."
                minOrderCostForm(
                    menuCost,
                    basketInfo.minOrderCost
                )
            } else {
                orderButton.setBackgroundResource(R.drawable.bg_round_27dp_main_color)
                orderButton.isClickable = true
                contentTextView.text = "결제 시 쿠폰 적용이 가능합니다."
                minOrderCostTextView.isVisible = false
            }
            basketViewPager.post {
                basketViewPager.setPadding(
                    0,
                    0,
                    0,
                    if (footerForm.isVisible) SizeUtils.getMeasuredHeight(footerForm) else 0
                )
            }
        }
    }

    /**
     * 탭 위치 초기화
     */
    private fun initTabPosition() {
        val position = viewModel.serviceTabPositionLiveData.value ?: findTabPosition(
            args.serviceTypeId,
            args.deliveryTypeId
        )
        with(binding.basketViewPager) {
            viewModel.serviceTabPositionLiveData.observe(viewLifecycleOwner) {
                if (it != null) setCurrentItem(it, false)
            }
            doOnPreDraw {
                viewModel.setServiceTabPosition(position)
            }
        }
    }

    private fun findTabPosition(serviceTypeId: Int, deliveryTypeId: Int): Int =
        when (serviceTypeId) {
            ServiceType.FOOD_DELIVERY.id -> {
                when (deliveryTypeId) {
                    DeliveryType.INSTANT.id -> 0
                    DeliveryType.PACKAGING.id -> 1
                    else -> 0
                }
            }

            ServiceType.SHOPPING_MALL.id -> 2
            else -> 0
        }

    private fun buildTabTitle(
        serviceTypeId: Int,
        deliveryTypeId: Int,
        basketCount: Int
    ): SpannableString = SpannableString(
        buildString {
            append(
                when {
                    serviceTypeId == ServiceType.FOOD_DELIVERY.id && deliveryTypeId == DeliveryType.INSTANT.id -> "배달"
                    serviceTypeId == ServiceType.FOOD_DELIVERY.id && deliveryTypeId == DeliveryType.PACKAGING.id -> "포장"
                    serviceTypeId == ServiceType.SHOPPING_MALL.id && deliveryTypeId == DeliveryType.PARCEL.id -> "택배배송"
                    else -> return@buildString
                }
            )
            append("\n${basketCount}")
        }
    ).apply {
        val newlineIndex = indexOf("\n")
        if (newlineIndex >= 0) {
            setSpan(
                AbsoluteSizeSpan(14, true),
                0,
                newlineIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            setSpan(
                AbsoluteSizeSpan(16, true),
                newlineIndex + 1,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun calculateBasketCount(serviceTypeId: Int, deliveryTypeId: Int): Int =
        basketListViewModel.getBasketList(serviceTypeId, deliveryTypeId)
            .sumOf { it.menuList.size }

    private fun minOrderCostForm(menuCost: Int, minOrderCost: Int) {
        debugLog("장바구니 최소주문금액 폼 설정", "선택된 메뉴금액 : $menuCost, 최소주문금액: $minOrderCost")
        with(binding) {
            if (menuCost < minOrderCost || menuCost == 0) {
                orderButton.setBackgroundResource(R.drawable.bg_round_27dp_disabled_color)
                orderButton.isClickable = false
                minOrderCostTextView.text = "최소주문금액은 ${
                    StringUtils.getString(
                        R.string.commonCost,
                        minOrderCost.toMoneyFormat()
                    )
                } 입니다."
                minOrderCostTextView.isVisible = true
            } else {
                orderButton.setBackgroundResource(R.drawable.bg_round_27dp_main_color)
                orderButton.isClickable = true
                minOrderCostTextView.isVisible = false
            }
        }
    }

    private fun updateTab(serviceTypeId: Int, deliveryTypeId: Int) {
        val tabPosition = findTabPosition(serviceTypeId, deliveryTypeId)
        val basketCount = calculateBasketCount(serviceTypeId, deliveryTypeId)
        val tabTitle = buildTabTitle(serviceTypeId, deliveryTypeId, basketCount)
        with(binding.basketTabLayout) {
            setTextAtChild(tabPosition, tabTitle)
            setTextAppearanceAtChild(
                tabPosition,
                if (tabPosition != selectedTabPosition) R.style.tabUnselectedTextAppearance else R.style.tabSelectedTextAppearance
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSelectedBasketShop(event: ChangeSelectedBasketShopEvent) {
        basketListViewModel.setSelectedBasketShop(event.old, event.basketShop)
        refreshFooter(event.basketShop)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeBasketMenu(event: ChangeBasketMenuEvent) {
        refreshFooter(event.basketShop)
        basketListViewModel.setMenu(event.basketMenu)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveBasketMenu(event: RemoveBasketMenuEvent) {
        basketListViewModel.removeMenu(event.basketMenu)
        val menuList = event.basketShop.menuList
        if (menuList.isEmpty()) {
            val basketInfoMap = basketListViewModel.basketInfoMap ?: return
            val basketInfo = basketInfoMap[event.basketShop.shopId] ?: return
            minOrderCostForm(0, basketInfo.minOrderCost)
        }
    }

    /**
     * 탭이 변경됐을 경우 하단 주문하기 폼이 보일지 안보일지를 결정한다.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeBasketTab(event: ChangeBasketTabEvent) {
        tabServiceType = event.serviceType
        tabDeliveryType = event.deliveryType
        if (event.basketShop != null) {
            refreshFooter(event.basketShop)
        } else {
            binding.footerForm.isVisible = false
        }
    }
}