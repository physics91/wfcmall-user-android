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
import com.theone.busandbt.databinding.FragmentNewShopOrMenuListBinding
import com.theone.busandbt.dto.shop.NewShopListItem
import com.theone.busandbt.fragment.SingleListFragment
import com.theone.busandbt.adapter.shop.NewShopAllViewListAdapter
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import com.theone.busandbt.view.recyclerview.decoration.VerticalSpaceItemDecoration
import org.koin.android.ext.android.inject
import retrofit2.Call

/**
 * 신규매장 모두보기 프래그먼트
 */
class NewShopListFragment :
    SingleListFragment<FragmentNewShopOrMenuListBinding, NewShopAllViewListAdapter, NewShopListItem>(),
    EnabledGoBackButton, RequiredDeliveryAddress {
    override val layoutId: Int = R.layout.fragment_new_shop_or_menu_list
    override val actionBarTitle: String = "신규 오픈 매장"

    override val recyclerView: RecyclerView get() = binding.itemList
    private val args by navArgs<NewShopListFragmentArgs>()
    private val shopAPI: ShopAPI by inject()

    override fun makeAdapter(): NewShopAllViewListAdapter = NewShopAllViewListAdapter()

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
        recyclerViewAdapter.setOnItemClick { _, _, item ->
            val action =
                NewShopListFragmentDirections.actionNewShopListFragmentToShopDetailsFragment(
                    item.id,
                    args.serviceTypeId,
                    args.deliveryTypeId
                )
            findNavController().navigate(action)
        }
    }

    override fun getCall(): Call<List<NewShopListItem>> {
        val da = deliveryAddressViewModel.selectedDeliveryAddress ?: error("부절적한 접근")
        return shopAPI.getNewShopList(page, 16, da.jibun, args.serviceTypeId, args.deliveryTypeId)
    }

    override fun onReceiveEmptyList(page: Int) {
        with(binding) {
            if (page == 1) {
                notExistPopularShopImageView.root.isVisible = true
            }
        }
    }
}