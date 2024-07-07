package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.CategoryRecyclerViewListAdapter
import com.theone.busandbt.adapter.shop.DiscountShopListAdapter
import com.theone.busandbt.adapter.shop.NewShopListAdapter
import com.theone.busandbt.adapter.shop.PopularShopListAdapter
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentFoodMainInstantTabBinding
import com.theone.busandbt.dto.shop.DiscountShopListItem
import com.theone.busandbt.dto.shop.NewShopListItem
import com.theone.busandbt.dto.shop.PopularShopListItem
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.model.category.CategoryViewModel
import com.theone.busandbt.model.main.FoodInstantMainViewModel
import com.theone.busandbt.model.main.ServiceMainViewModel
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * 음식점 배달주문 탭 화면
 * [ServiceMainFragment]의 TabLayout 안에 표시될 Fragment
 * TODO - 이 화면에서 주소 변경했을때 신규, 할인, 인기 매장 리셋 안됨
 */
class FoodMainInstantTabFragment : DataBindingFragment<FragmentFoodMainInstantTabBinding>() {
    override val layoutId: Int = R.layout.fragment_food_main_instant_tab
    private val categoryViewModel: CategoryViewModel by activityViewModel()
    private val viewModel: FoodInstantMainViewModel by viewModels()
    private val parentViewModel: ServiceMainViewModel by viewModels({ requireParentFragment() })
    private val shopAPI: ShopAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectedDeliveryAddress = deliveryAddressViewModel.selectedDeliveryAddress ?: return
        with(binding) {
            rootScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                parentViewModel.setScrollY(
                    scrollY
                )
            })
            newShopRecyclerView.addItemDecoration(
                VerticalSpaceItemDecoration(
                    SizeUtils.dp2px(
                        13F
                    )
                )
            )
            viewModel.popularShopListLiveData.observe(viewLifecycleOwner) {
                if (it != null) initPopularShopList(it)
            }
            viewModel.discountShopListLiveData.observe(viewLifecycleOwner) {
                if (it != null) initDiscountShopList(it)
            }
            viewModel.newShopListLiveData.observe(viewLifecycleOwner) {
                if (it != null) initNewShopList(it)
            }
            if (viewModel.popularShopListLiveData.value == null) {
                safeApiRequest(
                    shopAPI.getPopularShopList(
                        selectedDeliveryAddress.jibun,
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.INSTANT.id
                    ),
                    onFail = { _, _ ->
                        popularShopListForm.isVisible = false
                        notExistPopularShopImageView.root.isVisible = true
                    }
                ) {
                    viewModel.setPopularShopList(it)
                }
            }

            if (viewModel.discountShopListLiveData.value == null) {
                safeApiRequest(
                    shopAPI.getDiscountShopList(
                        1,
                        15,
                        selectedDeliveryAddress.jibun,
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.INSTANT.id
                    ),
                    onFail = { _, _ ->
                        discountShopListForm.isVisible = false
                        notExistDiscountShopImageView.root.isVisible = true
                    }
                ) {
                    viewModel.setDiscountShopList(it)
                }
            }

            if (viewModel.newShopListLiveData.value == null) {
                safeApiRequest(
                    shopAPI.getNewShopList(
                        1,
                        15,
                        selectedDeliveryAddress.jibun,
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.INSTANT.id
                    ),
                    onFail = { _, _ ->
                        newShopListForm.isVisible = false
                        notExistNewShopImageView.root.isVisible = true
                    }
                ) {
                    viewModel.setNewShopList(it)
                }
            }
            newOpenTextView.setOnClickListener {
                val action =
                    ServiceMainFragmentDirections.actionRestaurantMainFragmentToPopulaViewAllFragment(
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.INSTANT.id
                    )
                findNavController().navigate(action)
            }

            couponTextView.setOnClickListener {
                val action =
                    ServiceMainFragmentDirections.actionRestaurantMainFragmentToCouponViewAllFragment(
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.INSTANT.id
                    )
                findNavController().navigate(action)
            }

            newOpenTextView.text = buildSpannedString {
                append(
                    "#신규 오픈",
                    ForegroundColorSpan(ColorUtils.getColor(R.color.mainColor)),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append(" 가게에서 골라봐!")
            }

            couponTextView.text = buildSpannedString {
                append(
                    "#할인",
                    ForegroundColorSpan(ColorUtils.getColor(R.color.mainColor)),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("받고 야무지게 주문하자!")
            }

            popularTitleTextView.text = buildSpannedString {
                append("뭐 먹을지 고민이라면?! ")
                append(
                    "#인기맛집",
                    ForegroundColorSpan(ColorUtils.getColor(R.color.mainColor)),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            initCategory()
        }
    }

    override fun onResume() {
        super.onResume()
        appViewModel.basketServiceType = ServiceType.FOOD_DELIVERY
        appViewModel.basketDeliveryType = DeliveryType.INSTANT
    }

    private fun initCategory() {
        with(binding) {
            categoryRecyclerView.apply {
                adapter = CategoryRecyclerViewListAdapter(
                    categoryViewModel.foodDeliveryCategoryList,
                    ServiceType.FOOD_DELIVERY,
                    DeliveryType.INSTANT
                )
                addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(17F)))
            }
        }
    }

    private fun initPopularShopList(data: List<PopularShopListItem>) {
        with(binding) {
            if (data.isNotEmpty()) {
                popularListRecyclerView.addItemDecoration(
                    VerticalSpaceItemDecoration(
                        SizeUtils.dp2px(
                            11F
                        )
                    )
                )
                popularListRecyclerView.adapter = PopularShopListAdapter(data)
                return
            }
            popularShopListForm.isVisible = false
            notExistPopularShopImageView.root.isVisible = true
        }
    }

    private fun initDiscountShopList(data: List<DiscountShopListItem>) {
        with(binding) {
            if (data.isNotEmpty()) {
                discountShopListRecyclerView.addItemDecoration(
                    VerticalSpaceItemDecoration(
                        SizeUtils.dp2px(10F)
                    )
                )
                discountShopListRecyclerView.adapter = DiscountShopListAdapter(data).apply {
                    setOnItemClick { v, _, item ->
                        val action =
                            ServiceMainFragmentDirections.actionRestaurantMainFragmentToShopDetailsFragment(
                                item.id,
                                ServiceType.FOOD_DELIVERY.id,
                                DeliveryType.INSTANT.id
                            )
                        v.findNavController().navigate(action)
                    }
                }
                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(discountShopListRecyclerView)
                return
            }
            discountShopListForm.isVisible = false
            notExistDiscountShopImageView.root.isVisible = true
        }
    }

    private fun initNewShopList(data: List<NewShopListItem>) {
        with(binding) {
            if (data.isNotEmpty()) {
                newShopRecyclerView.adapter = NewShopListAdapter(data).apply {
                    setOnItemClick { v, _, item ->
                        val action =
                            ServiceMainFragmentDirections.actionRestaurantMainFragmentToShopDetailsFragment(
                                item.id,
                                ServiceType.FOOD_DELIVERY.id,
                                DeliveryType.INSTANT.id
                            )
                        v.findNavController().navigate(action)
                    }
                }
                return
            }
            newShopListForm.isVisible = false
            notExistNewShopImageView.root.isVisible = true
        }
    }
}