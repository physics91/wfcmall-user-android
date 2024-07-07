package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.databinding.FragmentNewShopOrMenuListBinding
import com.theone.busandbt.dto.menu.NewAndPopularMallMenuListItem
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.adapter.menu.NewMallMenuListAdapter
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.theone.busandbt.fragment.menu.MallMenuDetailsFragmentArgs
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 쇼핑몰 신규상품 모두보기 프래그먼트
 */
class ShoppingNewMenuFragment :
    SingleListFragment<FragmentNewShopOrMenuListBinding, NewMallMenuListAdapter, NewAndPopularMallMenuListItem>(),
    EnabledGoBackButton, RequiredDeliveryAddress {
    override val actionBarTitle: String = "신규 상품"
    override val layoutId: Int = R.layout.fragment_new_shop_or_menu_list
    override val recyclerView: RecyclerView get() = binding.itemList
    private val menuAPI: MenuAPI by inject()

    override fun makeAdapter(): NewMallMenuListAdapter = NewMallMenuListAdapter(childFragmentManager)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(21F)))
        recyclerView.addItemDecoration(
            VerticalSpaceItemDecoration(
                SizeUtils.dp2px(16F),
                doLast = false,
                doFirst = false
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

    override fun getCall(): Call<List<NewAndPopularMallMenuListItem>> {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: error("부절적한 접근")
        return menuAPI.getNewMallMenuList(
            page,
            15,
            jibun = da.jibun
        )
    }

    override fun onReceiveEmptyList(page: Int) {
        with(binding) {
            if (page == 1) {
                notExistPopularShopImageView.root.isVisible = true
            }
        }
    }
}