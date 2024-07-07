package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.PagerSnapHelper
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.CategoryType
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.CategoryRecyclerViewListAdapter
import com.theone.busandbt.adapter.menu.NewAndPopularMallMenuListAdapter
import com.theone.busandbt.adapter.shop.DiscountShoppingMallListAdapter
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentShoppingMainTabBinding
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.fragment.menu.MallMenuDetailsFragmentArgs
import com.theone.busandbt.model.category.CategoryViewModel
import com.theone.busandbt.model.main.MallMainViewModel
import com.theone.busandbt.model.main.ServiceMainViewModel
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * 전통시장 바로배달 메인 프래그먼트
 */
class ShoppingMainTabFragment :
    DataBindingFragment<FragmentShoppingMainTabBinding>() {
    override val layoutId: Int = R.layout.fragment_shopping_main_tab
    private val categoryViewModel: CategoryViewModel by activityViewModel()
    private val viewModel: MallMainViewModel by viewModels()
    private val parentViewModel: ServiceMainViewModel by viewModels({ requireParentFragment() })
    private val shopAPI: ShopAPI by inject()
    private val menuAPI: MenuAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            initPopularMenuList()
            initDiscountShopList()
            initNewMenuList()
            initEventListener()
            initCategory()
            rootScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                parentViewModel.setScrollY(
                    scrollY
                )
            })

            //#오픈 글자색을 바꿔준다
            newOpenTextView.text = buildSpannedString {
                append(
                    "#신규 상품",
                    ForegroundColorSpan(ColorUtils.getColor(R.color.mainColor)),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append("만 모았어요")
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
                append(
                    "#인기상품",
                    ForegroundColorSpan(ColorUtils.getColor(R.color.mainColor)),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                append(" 랭킹 TOP10")
            }
        }
    }

    private fun initEventListener() {
        with(binding) {
            allGoods.setOnClickListener {
                it.navigate(R.id.mall_menu_list_graph)
            }

            popularTitleTextView.setOnClickListener {
                it.navigate(
                    R.id.shopping_new_menu_list_graph, ShoppingNewMenuFragmentArgs(
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id
                    ).toBundle()
                )
            }

            // 할인매장 전체보기
            couponTextView.setOnClickListener {
                it.navigate(
                    R.id.discount_mall_graph, DiscountShoppingMallListFragmentArgs(
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id
                    ).toBundle()
                )
            }
        }
    }

    private fun initCategory() {
        with(binding) {
            categoryRecyclerView.apply {
                adapter = CategoryRecyclerViewListAdapter(
                    categoryViewModel.mallCategoryList.filter { it.type == CategoryType.LARGE.id },
                    ServiceType.SHOPPING_MALL,
                    DeliveryType.PARCEL
                )
                addItemDecoration(
                    HorizontalSpaceItemDecoration(
                        SizeUtils.dp2px(
                            17F
                        )
                    )
                )
            }
        }
    }

    private fun initNewMenuList() {
        with(binding) {
            viewModel.newMenuListLiveData.observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                if (it.isNotEmpty()) {
                    newShopRecyclerView.adapter =
                        NewAndPopularMallMenuListAdapter(childFragmentManager).apply {
                            addItems(it)
                            setOnItemClick { view, _, item ->
                                view.navigate(
                                    R.id.menu_detail_graph,
                                    MallMenuDetailsFragmentArgs(
                                        item.id,
                                        item.shopId,
                                        item.shopName,
                                        ServiceType.SHOPPING_MALL.id,
                                        DeliveryType.PARCEL.id,
                                        0
                                    ).toBundle()
                                )
                            }
                        }
                    newShopRecyclerView.addItemDecoration(
                        VerticalSpaceItemDecoration(
                            SizeUtils.dp2px(
                                12F
                            )
                        )
                    )
                    return@Observer
                }
                newShopListForm.isVisible = false
                notExistNewShopImageView.root.isVisible = true
            })
            val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
            if (viewModel.newMenuListLiveData.value == null) {
                safeApiRequest(
                    menuAPI.getNewMallMenuList(
                        1,
                        15,
                        jibun = da.jibun
                    ),
                    onFail = { _, _ ->
                        newShopListForm.isVisible = false
                        notExistNewShopImageView.root.isVisible = true
                    }
                ) {
                    viewModel.setNewMenuList(it)
                }
            }
        }
    }

    private fun initDiscountShopList() {
        with(binding) {
            viewModel.discountShopListLiveData.observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                if (it.isNotEmpty()) {
                    discountShopListRecyclerView.adapter = DiscountShoppingMallListAdapter().apply {
                        addItems(it)
                        setOnItemClick { view, _, item ->
                            view.navigate(
                                R.id.shoppingdetails_graph,
                                ShoppingDetailFragmentArgs(
                                    item.id,
                                    ServiceType.SHOPPING_MALL.id,
                                    DeliveryType.PARCEL.id
                                ).toBundle()
                            )
                        }
                    }
                    discountShopListRecyclerView.addItemDecoration(
                        VerticalSpaceItemDecoration(
                            SizeUtils.dp2px(10F)
                        )
                    )
                    val snapHelper = PagerSnapHelper()
                    snapHelper.attachToRecyclerView(discountShopListRecyclerView)
                    return@Observer
                }
                discountShopListForm.isVisible = false
                notExistDiscountShopImageView.root.isVisible = true
            })
            val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
            if (viewModel.discountShopListLiveData.value == null) {
                safeApiRequest(
                    shopAPI.getDiscountShopList(
                        1,
                        15,
                        da.jibun,
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id
                    ),
                    onFail = { _, _ ->
                        discountShopListForm.isVisible = false
                        notExistDiscountShopImageView.root.isVisible = true
                    }
                ) {
                    viewModel.setDiscountShopList(it)
                }
            }
        }
    }

    private fun initPopularMenuList() {
        with(binding) {
            viewModel.popularMenuListLiveData.observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                if (it.isNotEmpty()) {
                    popularListRecyclerView.adapter =
                        NewAndPopularMallMenuListAdapter(childFragmentManager).apply {
                            addItems(it)
                            setOnItemClick { view, _, item ->
                                view.navigate(
                                    R.id.menu_detail_graph,
                                    MallMenuDetailsFragmentArgs(
                                        item.id,
                                        item.shopId,
                                        item.shopName,
                                        ServiceType.SHOPPING_MALL.id,
                                        DeliveryType.PARCEL.id,
                                        0
                                    ).toBundle()
                                )
                            }
                        }
                    popularListRecyclerView.addItemDecoration(
                        VerticalSpaceItemDecoration(
                            SizeUtils.dp2px(
                                12F
                            )
                        )
                    )
                    return@Observer
                }
                popularShopListForm.isVisible = false
                notExistPopularShopImageView.root.isVisible = true
            })
            val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
            if (viewModel.popularMenuListLiveData.value == null) {
                safeApiRequest(
                    menuAPI.getPopularMallMenuList(
                        jibun = da.jibun
                    ),
                    onFail = { _, _ ->
                        newShopListForm.isVisible = false
                        notExistPopularShopImageView.root.isVisible = true
                    }
                ) {
                    viewModel.setPopularMenuList(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appViewModel.basketServiceType = ServiceType.SHOPPING_MALL
        appViewModel.basketDeliveryType = DeliveryType.PARCEL
    }
}