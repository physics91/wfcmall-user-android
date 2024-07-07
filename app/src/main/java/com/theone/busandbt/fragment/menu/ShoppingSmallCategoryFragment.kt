package com.theone.busandbt.fragment.menu

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ColorUtils
import com.busandbt.code.CategoryType
import com.busandbt.code.MenuSortType
import com.busandbt.code.ServiceType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.shop.MallSmallCategoryTabAdapter
import com.theone.busandbt.databinding.FragmentShoppingSmallCategoryBinding
import com.theone.busandbt.dialog.selection.MenuSortTypeSelectionDialog
import com.theone.busandbt.dto.Selection
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.eventbus.menu.DoInitMallMenuListPagerFragmentEvent
import com.theone.busandbt.eventbus.menu.DoInitMallMenuSmallPagerFragmentEvent
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.extension.getParcelableArrayCompat
import com.theone.busandbt.extension.getParcelableCompat
import com.theone.busandbt.extension.setOnReceiveData
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.instance.MAIN_TEXT_COLOR
import com.theone.busandbt.model.menu.MallMenuListViewModel
import com.theone.busandbt.model.menu.MallMenuSmallCategoryViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 전통시장 바로배달 먹거리 -> 음식 카테고리 탭 프래그먼트
 */
class ShoppingSmallCategoryFragment : DataBindingFragment<FragmentShoppingSmallCategoryBinding>() {
    override val layoutId: Int = R.layout.fragment_shopping_small_category
    private val viewType: Int by lazy {
        val args = arguments ?: error("")
        args.getInt("viewType")
    }
    private val menuSortTypeInputList: List<Selection<MenuSortType>> by lazy {
        MenuSortType.values().filter { it.id > 0 }.map { Selection(it.desc(), it) }
    }
    private val menuSortTypeSelectionDialog: MenuSortTypeSelectionDialog by lazy {
        MenuSortTypeSelectionDialog(menuSortTypeInputList)
    }
    private val parentViewModel: MallMenuListViewModel by activityViewModels()
    private val viewModel: MallMenuSmallCategoryViewModel by viewModels()
    private val baseCategory: CategoryListItem by lazy {
        val args = arguments ?: error("")
        args.getParcelableCompat("category")!!
    }
    private var isInit = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isInit = false
    }

    fun init() {
        if (isInit) return
        isInit = true
        val args = arguments ?: return
        val navController = findNavController()
        val currentDest = navController.currentDestination?.id
        navController.addOnDestinationChangedListener { _, n, _ ->
            if (n.id != currentDest) {
                if (isAdded && !isDetached) viewModel.savedTabPosition = binding.tabLayout.selectedTabPosition
            }
        }
        val categoryList =
            args.getParcelableArrayCompat<CategoryListItem>("categoryList").toMutableList()
        categoryList.add(
            0,
            CategoryListItem(
                0,
                baseCategory.id.takeIf { it > 0 } ?: baseCategory.upperCategoryId,
                ServiceType.SHOPPING_MALL.id,
                "전체",
                CategoryType.SMALL.id,
                "",
                emptyList()
            )
        )
        with(binding) {
            tabLayout.eventRegistrationTabSelectedDifferenceFont(
                tabUnSelect = R.style.shoppingSmaillTabUnselectedTextAppearance,
                tabSelect = R.style.shoppingSmaillTabSelectedTextAppearance
            )
            tabLayout.isVisible = categoryList.size > 1
            when (viewType) {
                0 -> {
                    coupon.setOnClickListener {
                        val hasCoupon = if (!coupon.isSelected) {
                            coupon.setTextColor(MAIN_COLOR)
                            coupon.setBackgroundResource(R.drawable.bg_sort_select_true_main_sub_form)
                            coupon.isSelected = true
                            returnImg.isVisible = true
                            true
                        } else {
                            coupon.setTextColor(MAIN_TEXT_COLOR)
                            coupon.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                            coupon.isSelected = false
                            if (!sort.isSelected) returnImg.isVisible = false
                            null
                        }
                        parentViewModel.setHasCoupon(hasCoupon)
                    }
                    sort.setOnClickListener {
                        sort.isSelected = true
                        menuSortTypeSelectionDialog.show(
                            childFragmentManager,
                            null
                        )
                    }
                    returnImg.setOnClickListener {
                        sort.isSelected = false
                        sort.text = MenuSortType.RECENT_ADD.desc()
                        sort.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                        sort.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
                        sort.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_down_arrow,
                            0
                        )
                        coupon.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                        coupon.setTextColor(ColorUtils.getColor(R.color.mainTextColor))
                        coupon.isSelected = false
                        setInitFilterButton()
                        parentViewModel.setMenuSortType(MenuSortType.RECENT_ADD)
                        parentViewModel.setHasCoupon(null)
                    }
                } // 뱃지 필터가 있는 뷰 모드
                1 -> {
                    viewModel.menuCountLiveData.observe(viewLifecycleOwner) {
                        allCountTextView.text = "${it ?: 0}"
                    }
                    sortFormConstraintLayout.isVisible = false
                    sortGroup.isVisible = true

                    shoppingDetailSortTextView.setOnClickListener {
                        menuSortTypeSelectionDialog.show(
                            childFragmentManager,
                            null
                        )
                    }
                }
            }
            viewPager.adapter = MallSmallCategoryTabAdapter(
                this@ShoppingSmallCategoryFragment,
                args.getInt("shopId"),
                categoryList,
                viewType
            )
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                val category = categoryList[position]
                tab.text = category.name
                tab.tag = category
            }.attach()
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    EventBus.getDefault().post(DoInitMallMenuListPagerFragmentEvent(tab.position))
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}
            })
            viewPager.offscreenPageLimit = categoryList.size + 1
            viewPager.post {
                val position = viewModel.savedTabPosition ?: 0
                if ((tabLayout.selectedTabPosition.takeIf { it >= 0 } ?: 0) == position) EventBus.getDefault()
                    .post(DoInitMallMenuListPagerFragmentEvent(position)) else viewPager.setCurrentItem(
                    position,
                    false
                )
            }

            menuSortTypeSelectionDialog.setOnReceiveData(this@ShoppingSmallCategoryFragment) {
                if (it == null) return@setOnReceiveData
                when (viewType) {
                    0 -> {
                        sort.text = it.desc()
                        sort.setBackgroundResource(R.drawable.bg_sort_select_true_main_sub_form)
                        sort.setTextColor(MAIN_COLOR)
                        sort.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_down_arrow_main_color,
                            0
                        )
                        setInitFilterButton()
                    }

                    1 -> shoppingDetailSortTextView.text = it.desc()
                }
                parentViewModel.setMenuSortType(it)
            }
        }
    }

    /**
     * 현재 필터 상태에 따라 초기화 버튼의 상태를 지정한다.
     */
    private fun setInitFilterButton() {
        with(binding) {
            val flag =
                parentViewModel.menuSortType != MenuSortType.RECENT_ADD || parentViewModel.hasCoupon != null
            returnImg.isVisible = flag
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDoInitPagerFragment(event: DoInitMallMenuSmallPagerFragmentEvent) {
        val args = arguments ?: return
        if (args.getInt("position") == event.position) init()
    }
}