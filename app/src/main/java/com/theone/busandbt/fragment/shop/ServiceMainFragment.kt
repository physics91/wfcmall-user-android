package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.busandbt.code.DeliveryType
import com.busandbt.code.PromotionStatus
import com.busandbt.code.PromotionType
import com.busandbt.code.ServiceType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.promotion.BannerViewPagerAdapter
import com.theone.busandbt.adapter.shop.ServiceMainPagerAdapter
import com.theone.busandbt.api.orderchannel.PromotionAPI
import com.theone.busandbt.databinding.FragmentServiceMainBinding
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.basket.BasketMainFragmentArgs
import com.theone.busandbt.fragment.promotion.PromotionDetailFragmentArgs
import com.theone.busandbt.model.BannerViewModel
import com.theone.busandbt.model.main.ServiceMainViewModel
import com.theone.busandbt.type.PromotionScreenType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


/**
 * 탭을 클릭 했을때 탭 위로는 유지한채 탭 밑으로는 배너 ,카테고리 등등 나머지 화면들 전부 다른 프래그먼트로 바뀐다
 */
class ServiceMainFragment : DataBindingFragment<FragmentServiceMainBinding>(),
    View.OnClickListener {

    override val layoutId: Int = R.layout.fragment_service_main
    private val args by navArgs<ServiceMainFragmentArgs>()
    private val viewModel: ServiceMainViewModel by viewModels()
    private lateinit var bannerViewPagerAdapter: BannerViewPagerAdapter
    private lateinit var bannerViewModel: BannerViewModel
    private val promotionAPI: PromotionAPI by inject()

    // TODO - 탭을 좀 더 유동적으로 확장이 될 수 있게 변경해야된다.
    private val deliveryOrderTab = SpannableString("배달")
    private val packingOrderTab = SpannableString("포장")
    private val shoppingOrderTab = SpannableString("택배배송")
    private val tabTitleArray = arrayOf(
        deliveryOrderTab, packingOrderTab, shoppingOrderTab
    )
    private val tabDrawableResIdArray = arrayOf(
        R.drawable.ic_delivery_tab,
        R.drawable.ic_packaging_tab,
        R.drawable.ic_parcel_tab
    )

    override fun onPause() {
        super.onPause()
        with(binding) {
            viewModel.setServiceTabPosition(foodMainTabLayout.selectedTabPosition)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbarInclude.goBackButton.setOnClickListener(this@ServiceMainFragment)
            toolbarInclude.goBasketButton.setOnClickListener(this@ServiceMainFragment)
            toolbarInclude.addressTextView.setOnClickListener(this@ServiceMainFragment)
            homeHitBox.setOnClickListener(this@ServiceMainFragment)
            likedHitBox.setOnClickListener(this@ServiceMainFragment)
            orderListHitBox.setOnClickListener(this@ServiceMainFragment)
            myInfoHitBox.setOnClickListener(this@ServiceMainFragment)
            searchView.setOnClickListener(this@ServiceMainFragment)
            foodMainTabLayout.eventRegistrationTabSelectedDifferenceFont()
            initTabLayout()
            initEventBanner(foodMainViewPager.currentItem)
            viewModel.serviceTabPositionLiveData.observe(viewLifecycleOwner) {
                if (it != null) foodMainViewPager.setCurrentItem(it, false)
            }
            bannerForm.post {
                if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    val mh = bannerForm.height
                    viewModel.scrollYLiveData.observe(viewLifecycleOwner) {
                        if (it == null) return@observe
                        val factor = 1.0f - it / 1000f
                        val newHeight = mh * factor
                        val targetView = bannerForm
                        // 높이를 0보다 작게 설정하지 않도록 합니다.
                        if (newHeight > 0) {
                            val layoutParams = targetView.layoutParams
                            layoutParams.height = newHeight.toInt()
                            targetView.layoutParams = layoutParams
                            targetView.isVisible = true
                        } else {
                            targetView.isVisible = false
                        }
                    }
                    viewModel.isBannerFormVisibleLiveData.observe(viewLifecycleOwner) {
                        if (it == null) return@observe
                        bannerForm.isVisible = it
                    }
                }
                viewModel.isBannerFormVisibleLiveData.observe(viewLifecycleOwner) {
                    if (it == null) return@observe
                    bannerForm.isVisible = it
                }
            }

            basketListViewModel.observe(requireActivity()) {
                var totalCount = 0
                it.forEach { shop ->
                    totalCount += shop.menuList.size
                }
                toolbarInclude.basketCountTextView.text = "$totalCount"
                if (toolbarInclude.basketGroup.isVisible) {
                    toolbarInclude.basketCountTextView.isVisible =
                        totalCount != 0
                }
            }
            deliveryAddressViewModel.observeSelectedAddress(requireActivity()) {
                toolbarInclude.addressTextView.text = it?.road?.ifEmpty { it.jibun } ?: "주소 등록하기"
            }

            childFragmentManager.setFragmentResultListener(
                "scrollLock",
                this@ServiceMainFragment
            ) { _, bundle ->
                val resultReceived = bundle.getBoolean("lock")
                foodMainViewPager.isUserInputEnabled = resultReceived
            }
        }
    }

    private fun getServiceTypeAndDeliveryType() = when (binding.foodMainViewPager.currentItem) {
        0 -> ServiceType.FOOD_DELIVERY.id to DeliveryType.INSTANT.id
        1 -> ServiceType.FOOD_DELIVERY.id to DeliveryType.PACKAGING.id
        2 -> ServiceType.SHOPPING_MALL.id to DeliveryType.PARCEL.id
        else -> ServiceType.FOOD_DELIVERY.id to DeliveryType.INSTANT.id
    }

    //선택에 따라 탭에 맞게 화면전환
    private fun initTabLayout() {
        with(binding) {
            foodMainViewPager.offscreenPageLimit = 3
            foodMainViewPager.adapter = ServiceMainPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(foodMainTabLayout, foodMainViewPager) { tab, position ->
                val cv = LayoutInflater.from(requireContext())
                    .inflate(R.layout.view_service_main_tab, null, false) as ConstraintLayout
                val tv = cv.findViewById<AppCompatTextView>(R.id.tabTextView)
                if (tv != null) {
                    tv.text = tabTitleArray[position]
                }
                cv.findViewById<AppCompatImageView>(R.id.tabImageView)
                    ?.setImageResource(tabDrawableResIdArray[position])
                cv.layoutParams = TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                cv.setPadding(0, 0, 0, 0)
                tab.customView = cv
            }.attach()
            foodMainTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab ?: return
                    initEventBanner(tab.position)
                    val cv = tab.customView as? ConstraintLayout ?: return
                    cv.isSelected = true
                    val tv = cv.findViewById<AppCompatTextView>(R.id.tabTextView)
                    if (tv != null) {
                        tv.isSelected = true
                        tv.typeface = ResourcesCompat.getFont(requireContext(), R.font.sult_bold)
                    }
                    cv.findViewById<AppCompatImageView>(R.id.tabImageView)?.isSelected = true

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab ?: return
                    val cv = tab.customView as? ConstraintLayout ?: return
                    cv.isSelected = false
                    val tv = cv.findViewById<AppCompatTextView>(R.id.tabTextView)
                    if (tv != null) {
                        tv.isSelected = false
                        tv.typeface = ResourcesCompat.getFont(requireContext(), R.font.sult_medium)
                    }
                    cv.findViewById<AppCompatImageView>(R.id.tabImageView)?.isSelected = false
                    initEventBanner(tab.position)
                }
            })

            val position = viewModel.serviceTabPositionLiveData.value ?: when (args.serviceTypeId) {
                ServiceType.FOOD_DELIVERY.id -> {
                    when (args.deliveryTypeId) {
                        DeliveryType.INSTANT.id -> 0
                        DeliveryType.PACKAGING.id -> 1
                        else -> 0
                    }
                }

                ServiceType.SHOPPING_MALL.id -> {
                    when (args.deliveryTypeId) {
                        DeliveryType.PARCEL.id -> 2
                        else -> 0
                    }
                }

                else -> 0
            }
            foodMainTabLayout.getTabAt(position)?.run {
                val textView = view.children.find { it is TextView } as? TextView ?: return
                textView.typeface = ResourcesCompat.getFont(requireContext(), R.font.sult_bold)
            }
            foodMainViewPager.doOnPreDraw {
                viewModel.setServiceTabPosition(position)
            }
        }
    }

    private fun initEventBanner(currentItem: Int) {
        with(binding) {
            bannerViewModel =
                ViewModelProvider(this@ServiceMainFragment)[BannerViewModel::class.java]
            bannerViewAllTextView.setOnClickListener {
                val action =
                    ServiceMainFragmentDirections.actionRestaurantMainFragmentToPromotionListFragment()
                findNavController().navigate(action)
            }
            viewModel.promotionListLiveData.observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                bannerViewPager.apply {
                    this.clipToOutline = true
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
                            pageNumberTextView.text = "${position + 1}"
                            //직접 유저가 스크롤했을 떄!
                            bannerViewModel.setCurrentPosition(position)
                        }
                    })
                }
                bannerViewModel.bannerItemList.observe(
                    viewLifecycleOwner
                ) { bannerItemList ->
                    bannerViewPagerAdapter.setItems(bannerItemList)
                }
                bannerViewModel.currentPosition.observe(
                    viewLifecycleOwner
                ) { currentPosition ->
////                        // TODO - 이 로직이 실행됐을때 스크롤이 자동으로 올라가는 문제가 생겨서 임시방편으로 처리해놓음
//                        if (rootScrollView.scrollY < rootScrollView.measuredHeight / 3) bannerViewPager.currentItem =
//                            currentPosition
                }
                bannerViewAllTextView.text = "/ ${it.size}"
                bannerViewModel.setBannerItems(it)
                registerTimerJob()
            })
            if (foodMainViewPager.currentItem == 0 || foodMainViewPager.currentItem != currentItem) {
                safeApiRequest(
                    promotionAPI.getPromotionList(
                        promotionType = PromotionType.BANNER.id,
                        promotionStatus = PromotionStatus.PROGRESS.id,
                        displayLocation = PromotionScreenType.fromTabPosition(currentItem)?.id
                            ?: PromotionScreenType.DELIVERY_TAB.id
                    )
                ) {
                    viewModel.setPromotionList(it)
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

    override fun onClick(view: View?) {
        with(binding) {
            when (view) {
                myInfoHitBox -> {
                    if (!loginInfoViewModel.isLoginState()) {
                        suggestLogin()
                        return
                    }
                    findNavController().navigate(ServiceMainFragmentDirections.actionRestaurantMainFragmentToMyInfoFragment())
                }

                orderListHitBox -> {
                    if (!loginInfoViewModel.isLoginState()) {
                        suggestLogin()
                        return
                    }
                    val (serviceTypeId, deliveryTypeId) = getServiceTypeAndDeliveryType()
                    val action =
                        ServiceMainFragmentDirections.actionRestaurantMainFragmentToOrderHistoryFragment(
                            serviceTypeId,
                            deliveryTypeId
                        )
                    findNavController().navigate(action)
                }

                homeHitBox -> findNavController().fixedPopBackStack(R.id.mainFragment)
                searchView -> {
                    val action =
                        ServiceMainFragmentDirections.actionRestaurantMainFragmentToSearchMainFragment()
                    findNavController().navigate(action)
                }

                likedHitBox -> {
                    if (!loginInfoViewModel.isLoginState()) {
                        suggestLogin()
                        return
                    }
                    val (serviceTypeId, deliveryTypeId) = getServiceTypeAndDeliveryType()
                    debugLog("서비스 유형", serviceTypeId)
                    debugLog("배달 유형", deliveryTypeId)
                    val action =
                        ServiceMainFragmentDirections.actionRestaurantMainFragmentToMyWishListFragment(
                            serviceTypeId,
                            deliveryTypeId
                        )
                    findNavController().navigate(action)
                }

                toolbarInclude.addressTextView -> {
                    toolbarInclude.addressTextView.navigate(R.id.delivery_address_list_graph)
                }

                toolbarInclude.goBackButton -> findNavController().popBackStack()
                toolbarInclude.goBasketButton -> {
                    val navController = findNavController()
                    val dest = navController.currentDestination ?: return
                    navController.navigate(
                        R.id.basket_graph,
                        BasketMainFragmentArgs(
                            appViewModel.basketServiceType.id,
                            appViewModel.basketDeliveryType.id
                        ).toBundle(),
                        NavOptions.Builder().setPopUpTo(dest.id, false).build()
                    )
                }

                else -> {}
            }
        }
    }
}