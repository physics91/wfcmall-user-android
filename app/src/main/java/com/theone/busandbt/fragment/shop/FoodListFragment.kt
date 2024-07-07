package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.google.android.material.tabs.TabLayoutMediator
import com.theone.busandbt.R
import com.theone.busandbt.adapter.shop.ShopListViewPagerAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EnabledGoBasketButton
import com.theone.busandbt.databinding.FragmentRestaurantListBinding
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.extension.eventRegistrationTabSelectedDifferenceFont
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.model.category.CategoryViewModel
import com.theone.busandbt.model.shop.FoodListFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * 음식점가게 리스트를 보여주는 메인화면이다
 */
class FoodListFragment : DataBindingFragment<FragmentRestaurantListBinding>(), EnabledGoBackButton,
    EnabledGoBasketButton {
    override val layoutId: Int = R.layout.fragment_restaurant_list
    override val actionBarTitle: String = "배달"

    private val args by navArgs<FoodListFragmentArgs>()
    private val categoryViewModel: CategoryViewModel by activityViewModel()
    private val foodListFragmentViewModel: FoodListFragmentViewModel by viewModels()
    private var isTabInit = false

    override fun onResume() {
        super.onResume()
        appViewModel.basketServiceType = ServiceType.FOOD_DELIVERY
        appViewModel.basketDeliveryType = DeliveryType.find(args.deliveryTypeId)
        isTabInit = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val shopListViewPagerAdapter = ShopListViewPagerAdapter(childFragmentManager, lifecycle)
            shopListViewPager.adapter = shopListViewPagerAdapter
            val categoryList = categoryViewModel.foodDeliveryCategoryList
            categoryList.run {
                shopListViewPagerAdapter.setItems(this)
                TabLayoutMediator(categoryTabLayout, shopListViewPager) { tab, position ->
                    tab.text = this[position].name
                }.attach()
                initTabPosition(this)
                if (foodListFragmentViewModel.currentCategoryIdLiveData.value == null) {
                    foodListFragmentViewModel.setCurrentCategoryId(args.categoryId)
                }
            }
            categoryTabLayout.eventRegistrationTabSelectedDifferenceFont()
        }
    }

    override fun onStop() {
        super.onStop()
        with(binding) {
            val adapter = shopListViewPager.adapter as? ShopListViewPagerAdapter ?: return
            foodListFragmentViewModel.setCurrentCategoryId(adapter.getCategory(shopListViewPager.currentItem).id)
        }
    }

    private fun initTabPosition(categoryList: List<CategoryListItem>) {
        foodListFragmentViewModel.currentCategoryIdLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val categoryTabIndex = categoryList.indexOfFirst { c -> c.id == it }
            if (categoryTabIndex == -1) return@observe
            with(binding.shopListViewPager) {
                setCurrentItem(categoryTabIndex, false)
//                doOnPreDraw {
//                    setCurrentItem(categoryTabIndex, false)
//                }
            }
        }
    }
}