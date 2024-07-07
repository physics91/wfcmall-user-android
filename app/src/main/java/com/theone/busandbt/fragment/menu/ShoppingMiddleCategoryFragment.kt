package com.theone.busandbt.fragment.menu

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.busandbt.code.CategoryType
import com.busandbt.code.ServiceType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.shop.MallMiddleCategoryPagerAdapter
import com.theone.busandbt.databinding.FragmentShoppingMiddleCategoryBinding
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.eventbus.menu.DoInitMallMenuMiddlePagerFragmentEvent
import com.theone.busandbt.eventbus.menu.DoInitMallMenuSmallPagerFragmentEvent
import com.theone.busandbt.extension.debugLog
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.extension.getParcelableArrayCompat
import com.theone.busandbt.extension.getParcelableCompat
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.model.menu.MallMenuMiddleCategoryViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 중분류 화면
 */
class ShoppingMiddleCategoryFragment :
    DataBindingFragment<FragmentShoppingMiddleCategoryBinding>() {
    override val layoutId: Int = R.layout.fragment_shopping_middle_category
    private val baseCategory: CategoryListItem by lazy {
        val args = arguments ?: error("")
        args.getParcelableCompat("category")!!
    }
    private var isInit = false
    private val viewModel: MallMenuMiddleCategoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isInit = false
    }

    fun init() {
        if (isInit) return
        with(binding) {
            val args = arguments ?: return
            tabLayout.eventRegistrationTabSelectedDifferenceFont(
                tabUnSelect = R.style.shoppingMiddleTabUnselectedTextAppearance,
                tabSelect = R.style.shoppingMiddleTabSelectedTextAppearance
            )
            val navController = findNavController()
            val currentDest = navController.currentDestination?.id
            navController.addOnDestinationChangedListener { _, n, _ ->
                if (n.id != currentDest) {
                    if (isAdded && !isDetached) viewModel.savedTabPosition = binding.tabLayout.selectedTabPosition
                }
            }
            val categoryList = arrayListOf(
                CategoryListItem(
                    0,
                    baseCategory.id,
                    ServiceType.SHOPPING_MALL.id,
                    "전체",
                    CategoryType.MIDDLE.id,
                    "",
                    emptyList()
                )
            )
            if (baseCategory.id > 0) {
                categoryList += args.getParcelableArrayCompat<CategoryListItem>("categoryList")
                    .toMutableList()
                viewPager.adapter =
                    MallMiddleCategoryPagerAdapter(
                        this@ShoppingMiddleCategoryFragment,
                        categoryList,
                        args.getInt("shopId"),
                        args.getInt("viewType")
                    )

                if (categoryList.size == 1) tabLayout.isVisible = false
                else {
                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        val category = categoryList[position]
                        tab.text = category.name
                        tab.tag = category
                    }.attach()
                    tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab) {
                            debugLog("미들탭선택", tab.position)
                            EventBus.getDefault().post(DoInitMallMenuSmallPagerFragmentEvent(tab.position))
                        }

                        override fun onTabReselected(tab: TabLayout.Tab) {}
                        override fun onTabUnselected(tab: TabLayout.Tab) {}
                    })
                }
            } else {
                viewPager.adapter = MallMiddleCategoryPagerAdapter(
                    this@ShoppingMiddleCategoryFragment,
                    categoryList,
                    args.getInt("shopId"),
                    args.getInt("viewType")
                )
                tabLayout.isVisible = false
            }
            viewPager.offscreenPageLimit = categoryList.size + 1
            viewPager.post {
                val position = viewModel.savedTabPosition ?: 0
                EventBus.getDefault()
                    .post(DoInitMallMenuSmallPagerFragmentEvent(position))
//                debugLog("미들 초기화 위치", position)
//                debugLog("미들 현재 탭 위치", tabLayout.selectedTabPosition)
//                if ((tabLayout.selectedTabPosition.takeIf { it >= 0 } ?: 0) == position) EventBus.getDefault()
//                    .post(DoInitMallMenuSmallPagerFragmentEvent(position)) else viewPager.setCurrentItem(position, false)
            }
        }
        isInit = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDoInitPagerFragment(event: DoInitMallMenuMiddlePagerFragmentEvent) {
        val args = arguments ?: return
        if (args.getInt("position") == event.position) init()
    }
}