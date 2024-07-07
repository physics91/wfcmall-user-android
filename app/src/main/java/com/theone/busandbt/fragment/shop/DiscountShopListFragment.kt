package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.databinding.FragmentDiscountShopListBinding
import com.theone.busandbt.dto.shop.DiscountShopListItem
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.adapter.shop.DiscountShopListAdapter
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 할인 쿠폰 매장 리스트 화면
 */
class DiscountShopListFragment :
    SingleListFragment<FragmentDiscountShopListBinding, DiscountShopListAdapter, DiscountShopListItem>(),
    EnabledGoBackButton, RequiredDeliveryAddress {
    override val layoutId: Int = R.layout.fragment_discount_shop_list
    override val actionBarTitle: String = "할인 쿠폰 매장"

    override val recyclerView: RecyclerView get() = binding.discountShopRecyclerView
    private val args by navArgs<DiscountShopListFragmentArgs>()
    private val shopAPI: ShopAPI by inject()

    override fun makeAdapter(): DiscountShopListAdapter = DiscountShopListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            lifecycleOwner = this@DiscountShopListFragment
            recyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(14F)))
            recyclerViewAdapter.setOnItemClick { _, _, item ->
                val action =
                    DiscountShopListFragmentDirections.actionDiscountShopListFragmentToShopDetailsFragment(
                        item.id,
                        args.serviceTypeId,
                        args.deliveryTypeId
                    )
                findNavController().navigate(action)
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