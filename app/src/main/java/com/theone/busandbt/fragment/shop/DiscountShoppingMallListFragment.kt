package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentDiscountShoppingMallListBinding
import com.theone.busandbt.dto.shop.DiscountShopListItem
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.adapter.shop.DiscountShoppingMallListAdapter
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.koin.android.ext.android.inject
import retrofit2.Call

class DiscountShoppingMallListFragment :
    SingleListFragment<FragmentDiscountShoppingMallListBinding, DiscountShoppingMallListAdapter, DiscountShopListItem>(),
    EnabledGoBackButton, RequiredDeliveryAddress {
    override val layoutId: Int = R.layout.fragment_discount_shopping_mall_list
    override val actionBarTitle: String = "할인 쿠폰 매장"

    override val recyclerView: RecyclerView get() = binding.discountShopRecyclerView
    private val args by navArgs<DiscountShopListFragmentArgs>()
    private val shopAPI: ShopAPI by inject()

    override fun makeAdapter(): DiscountShoppingMallListAdapter = DiscountShoppingMallListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            lifecycleOwner = this@DiscountShoppingMallListFragment
            recyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(14F)))
            recyclerViewAdapter.setOnItemClick { _, _, item ->
                view.navigate(
                    R.id.shoppingdetails_graph,
                    ShopDetailFragmentArgs(item.id, args.serviceTypeId, args.deliveryTypeId).toBundle()
                )
            }
        }
    }

    override fun getCall(): Call<List<DiscountShopListItem>> {
        return shopAPI.getDiscountShopList(
            page,
            15,
            deliveryAddressViewModel.selectedDeliveryAddress?.jibun ?: "",
            args.serviceTypeId,
            args.deliveryTypeId
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