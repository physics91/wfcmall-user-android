package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.busandbt.code.ShopSortType
import com.busandbt.code.ShopType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.shop.FoodListAdapter
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentRestaurantListItemBinding
import com.theone.busandbt.dialog.selection.MinOrderCostSelectionDialog
import com.theone.busandbt.dialog.selection.ShopSortTypeSelectionDialog
import com.theone.busandbt.dto.Selection
import com.theone.busandbt.dto.shop.ShopListItem
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.setOnReceiveData
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.instance.MAIN_TEXT_COLOR
import com.theone.busandbt.type.MinOrderCostType
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 가게리스트를 보여주는 화면이다.
 * TODO: 정렬기능들 처리해야함
 */
class FoodListItemFragment :
    SingleListFragment<FragmentRestaurantListItemBinding, FoodListAdapter, ShopListItem>(),
    View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_restaurant_list_item
    override val recyclerView: RecyclerView get() = binding.restaurantListRecyclerView
    override val isAutoLoad: Boolean = false
    private val args by navArgs<FoodListItemFragmentArgs>()
    private val shopAPI: ShopAPI by inject()
    private var coupon: Boolean? = null
    private var distance: Double? = null
    private var packaging: Boolean? = null
    private lateinit var shopSortTypeSelectionDialog: ShopSortTypeSelectionDialog
    private var selectedShopSortType: ShopSortType = ShopSortType.NEAR
    private lateinit var minOrderCostTypeSelectionDialog: MinOrderCostSelectionDialog
    private var selectedMinOrderCost: MinOrderCostType = MinOrderCostType.ALL
    private var isInit = false

    override fun makeAdapter(): FoodListAdapter = FoodListAdapter(DeliveryType.INSTANT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!viewModel.saved) isInit = false
        shopSortTypeSelectionDialog = ShopSortTypeSelectionDialog(
            ShopSortType.values().filter { it.id > 0 }.map { Selection(it.desc(), it) }
        )
        minOrderCostTypeSelectionDialog = MinOrderCostSelectionDialog()
        with(binding) {
            minOrderCostTextView.setOnClickListener(this@FoodListItemFragment)
            distanceOrderTextView.setOnClickListener(this@FoodListItemFragment)
            packagingBadgeTextView.setOnClickListener(this@FoodListItemFragment)
            couponBadgeTextView.setOnClickListener(this@FoodListItemFragment)
            resetImageView.setOnClickListener(this@FoodListItemFragment)

            recyclerViewAdapter.setOnItemClick { v, _, item ->
                v.navigate(
                    R.id.shop_detail_graph,
                    ShopDetailFragmentArgs(
                        item.id,
                        ServiceType.FOOD_DELIVERY.id,
                        DeliveryType.INSTANT.id
                    ).toBundle()
                )
            }

            minOrderCostTypeSelectionDialog.setOnReceiveData(this@FoodListItemFragment) {
                if (it == null) return@setOnReceiveData
                selectedMinOrderCost = it
                minOrderCostTextView.text = it.title
                minOrderCostTextView.setBackgroundResource(R.drawable.bg_address_choice_text)
                minOrderCostTextView.setTextColor(MAIN_COLOR)
                minOrderCostTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow_main_color,
                    0
                )
                setInitFilterButton()
                notExistShopForm.root.isVisible = false
                refresh()
            }

            shopSortTypeSelectionDialog.setOnReceiveData(this@FoodListItemFragment) {
                if (it == null) return@setOnReceiveData
                selectedShopSortType = it
                distanceOrderTextView.text = it.desc()
                distanceOrderTextView.setBackgroundResource(R.drawable.bg_address_choice_text)
                distanceOrderTextView.setTextColor(MAIN_COLOR)
                distanceOrderTextView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow_main_color,
                    0
                )
                setInitFilterButton()
                notExistShopForm.root.isVisible = false
                refresh()
            }
        }
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shopSortTypeSelectionDialog.recycle(this)
        minOrderCostTypeSelectionDialog.recycle(this)
    }

    override fun onReceiveEmptyList(page: Int) {
        with(binding) {
            if (page == 1) {
                notExistShopForm.root.isVisible = true
            }
        }
    }

    override fun getCall(): Call<List<ShopListItem>> {
        val da = deliveryAddressViewModel.selectedDeliveryAddress
        return shopAPI.getShopList(
            page,
            15,
            jibun = da?.jibun,
            categoryId = args.categoryId,
            shopSortType = selectedShopSortType.id,
            minOrderCost = if (selectedMinOrderCost == MinOrderCostType.ALL) null else selectedMinOrderCost.minOrderCost,
            distance = distance,
            customerLat = if (distance != null || selectedShopSortType == ShopSortType.NEAR) da?.lat else null,
            customerLng = if (distance != null || selectedShopSortType == ShopSortType.NEAR) da?.lng else null,
            coupon = if (binding.couponBadgeTextView.isSelected) true else null,
            packaging = if (binding.packagingBadgeTextView.isSelected) true else null,
            serviceType = ServiceType.FOOD_DELIVERY.id,
            deliveryType = DeliveryType.INSTANT.id,
            shopType = ShopType.NORMAL.id
        )
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view) {
                resetImageView -> {
                    this@FoodListItemFragment.coupon = null
                    packaging = null
                    selectedMinOrderCost = MinOrderCostType.ALL
                    selectedShopSortType = ShopSortType.NEAR
                    minOrderCostTextView.text = "최소주문금액"
                    minOrderCostTextView.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                    minOrderCostTextView.setTextColor(MAIN_TEXT_COLOR)
                    minOrderCostTextView.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_down_arrow,
                        0
                    )
                    distanceOrderTextView.text = "가까운 순"
                    distanceOrderTextView.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                    distanceOrderTextView.setTextColor(MAIN_TEXT_COLOR)
                    distanceOrderTextView.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_down_arrow,
                        0
                    )
                    couponBadgeTextView.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                    couponBadgeTextView.setTextColor(MAIN_TEXT_COLOR)
                    couponBadgeTextView.isSelected = false
                    packagingBadgeTextView.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                    packagingBadgeTextView.setTextColor(MAIN_TEXT_COLOR)
                    packagingBadgeTextView.isSelected = false
                    setInitFilterButton()
                    notExistShopForm.root.isVisible = false
                    refresh()
                }

                couponBadgeTextView -> {
                    if (!couponBadgeTextView.isSelected) {
                        couponBadgeTextView.setTextColor(MAIN_COLOR)
                        couponBadgeTextView.setBackgroundResource(R.drawable.bg_sort_select_true_main_sub_form)
                    } else {
                        couponBadgeTextView.setTextColor(MAIN_TEXT_COLOR)
                        couponBadgeTextView.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                    }
                    couponBadgeTextView.isSelected = !couponBadgeTextView.isSelected
                    setInitFilterButton()
                    notExistShopForm.root.isVisible = false
                    refresh()
                }

                packagingBadgeTextView -> {
                    if (!packagingBadgeTextView.isSelected) {
                        packagingBadgeTextView.setTextColor(MAIN_COLOR)
                        packagingBadgeTextView.setBackgroundResource(R.drawable.bg_sort_select_true_main_sub_form)
                    } else {
                        packagingBadgeTextView.setTextColor(MAIN_TEXT_COLOR)
                        packagingBadgeTextView.setBackgroundResource(R.drawable.bg_round_14dp_d5d5d5)
                    }
                    packagingBadgeTextView.isSelected = !packagingBadgeTextView.isSelected
                    setInitFilterButton()
                    notExistShopForm.root.isVisible = false
                    refresh()
                }

                distanceOrderTextView -> {
                    shopSortTypeSelectionDialog.setDefault(selectedShopSortType)
                    shopSortTypeSelectionDialog.show(childFragmentManager, null)
                }

                minOrderCostTextView -> {
                    minOrderCostTypeSelectionDialog.setDefault(selectedMinOrderCost)
                    minOrderCostTypeSelectionDialog.show(childFragmentManager, null)
                }
            }
        }
    }

    fun init() {
        if (isInit) return
        isInit = true
        refresh()
    }

    /**
     * 현재 필터 상태에 따라 초기화 버튼의 상태를 지정한다.
     */
    private fun setInitFilterButton() {
        with(binding) {
            val flag =
                selectedMinOrderCost != MinOrderCostType.ALL || selectedShopSortType != ShopSortType.NEAR || couponBadgeTextView.isSelected || packagingBadgeTextView.isSelected
            resetImageView.isVisible = flag
        }
    }
}