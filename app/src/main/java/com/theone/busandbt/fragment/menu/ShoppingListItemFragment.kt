package com.theone.busandbt.fragment.menu

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.menu.MallMenuListAdapter
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.databinding.FragmentShoppingListItemBinding
import com.theone.busandbt.dto.category.CategoryListItem
import com.theone.busandbt.dto.menu.MallMenuListItem
import com.theone.busandbt.eventbus.menu.DoInitMallMenuListPagerFragmentEvent
import com.theone.busandbt.extension.getParcelableCompat
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.model.menu.MallMenuListViewModel
import com.theone.busandbt.model.menu.MallMenuSmallCategoryViewModel
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 쇼핑몰 카테고리 리스트 아이템 프래그먼트
 */
class ShoppingListItemFragment :
    SingleListFragment<FragmentShoppingListItemBinding, MallMenuListAdapter, MallMenuListItem>(),
    EventBusSubscriber {
    override val layoutId: Int = R.layout.fragment_shopping_list_item
    override val recyclerView: RecyclerView get() = binding.menuListRecyclerView
    private val baseCategory: CategoryListItem by lazy {
        val args = arguments ?: error("")
        args.getParcelableCompat("category")!!
    }

    private val shopId: Int by lazy {
        val args = arguments ?: error("")
        args.getInt("shopId")
    }
    private val viewType: Int by lazy {
        val args = arguments ?: error("")
        args.getInt("viewType")
    }
    private val menuAPI: MenuAPI by inject()
    private val mallMenuMainViewModel: MallMenuListViewModel by activityViewModels()
    private val parentViewModel: MallMenuSmallCategoryViewModel by viewModels({ requireParentFragment() })

    override fun getCall(): Call<List<MallMenuListItem>> {
        val innerDeliveryAddress = deliveryAddressViewModel.selectedDeliveryAddress ?: error("")
        return menuAPI.getMallMenuList(
            page,
            20,
            jibun = innerDeliveryAddress.jibun,
            shopId = shopId.takeIf { it > 0 },
            categoryId = getActualCategoryId(),
            menuSortType = mallMenuMainViewModel.menuSortType?.id,
            coupon = mallMenuMainViewModel.hasCoupon
        )
    }

    override fun makeAdapter(): MallMenuListAdapter = MallMenuListAdapter(childFragmentManager)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(21F)))
        recyclerView.addItemDecoration(
            VerticalSpaceItemDecoration(
                SizeUtils.dp2px(16f),
                doFirst = false,
                doLast = false
            )
        )
        recyclerViewAdapter.setOnItemClick { v, _, item ->
            v.navigate(
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

    override fun onReceiveEmptyList(page: Int) {
        if (page == 1) {
            binding.notExistGroup.isVisible = true
        }
    }

    override fun refresh() {
        super.refresh()
        if (viewType == 1) {
            val da = deliveryAddressViewModel.selectedDeliveryAddress ?: return
            safeApiRequest(
                menuAPI.getMenuCount(
                    serviceType = ServiceType.SHOPPING_MALL.id,
                    coupon = mallMenuMainViewModel.hasCoupon,
                    jibun = da.jibun,
                    categoryId = getActualCategoryId(),
                    shopId = shopId.takeIf { it > 0 }
                )
            ) {
                parentViewModel.setMenuCount(it.count)
            }
        }
    }

    private fun getActualCategoryId(): Int? =
        baseCategory.id.takeIf { it > 0 } ?: baseCategory.upperCategoryId.takeIf { it > 0 }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDoInitPagerFragment(event: DoInitMallMenuListPagerFragmentEvent) {
        val args = arguments ?: return
        if (args.getInt("position") == event.position) initData()
    }
}