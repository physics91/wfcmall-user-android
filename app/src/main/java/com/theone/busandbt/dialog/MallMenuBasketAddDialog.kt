package com.theone.busandbt.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.theone.busandbt.R
import com.theone.busandbt.addon.RequiredDeliveryAddress
import com.theone.busandbt.api.orderchannel.MenuAPI
import com.theone.busandbt.databinding.DialogBasketBinding
import com.theone.busandbt.dto.cost.MenuCostListItem
import com.theone.busandbt.dto.menu.MenuOptionGroup
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.toMoneyFormat
import com.theone.busandbt.adapter.menu.MallMenuOptionGroupListAdapter
import com.theone.busandbt.model.BasketListViewModel
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theone.busandbt.extension.safeApiRequest
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.properties.Delegates

/**
 * 전통시장 장바구니 아이콘 클릭 시 나오는 다이얼로그
 */
class MallMenuBasketAddDialog : DataBindingBottomDialog<DialogBasketBinding>(),
    RequiredDeliveryAddress {
    override val layoutId: Int = R.layout.dialog_basket
    private val menuAPI: MenuAPI by inject()
    private val basketListViewModel: BasketListViewModel by activityViewModel()
    private var basketCount: Int by Delegates.observable(1) { _, _, newValue ->
        with(binding) {
            val mc = menuCostListItem
            countTextView.text = newValue.toString()
            confirmBtn.text =
                "${((mc.saleCost + optionCost) * newValue).toMoneyFormat()}원 장바구니 담기"
        }
    }
    private lateinit var menuCostListItem: MenuCostListItem
    private var optionCost: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        with(binding) {
            val menuCount = args.getInt("menuCount", 1)
            safeApiRequest(
                menuAPI.getMallMenuDetail(args.getInt("menuId"))
            ) {
                menuDetail = it
                optionCost = it.optionGroupList.flatMap { group ->
                    group.optionList.filter { option -> option.isSelected }
                }.sumOf { option -> option.cost }
                menuCostListItem = it.menuCostList.firstOrNull() ?: MenuCostListItem.createEmpty()
                val totalMenuCost = menuCostListItem.saleCost + optionCost
                confirmBtn.text = "${(totalMenuCost * basketCount).toMoneyFormat()}원 장바구니 담기"
                priceTextView.text = "${totalMenuCost.toMoneyFormat()}원"
                menuOptionListRecyclerView.adapter =
                    MallMenuOptionGroupListAdapter(it.optionGroupList)
                exitBtn.setOnClickListener {
                    dismiss()
                }
                countTextView.text = basketCount.toString()
                plusImageView.setOnClickListener {
                    if (basketCount >= 99) return@setOnClickListener
                    basketCount += 1
                }

                minusImageView.setOnClickListener {
                    if (basketCount <= 1) return@setOnClickListener
                    basketCount -= 1
                }
                confirmBtn.setOnClickListener { view ->
                    val adapter = menuOptionListRecyclerView.adapter as? MallMenuOptionGroupListAdapter
                        ?: return@setOnClickListener
                    val optionGroupList = ArrayList<MenuOptionGroup>()
                    adapter.items.forEach { optionGroup ->
                        optionGroupList.add(optionGroup.copy(optionList = optionGroup.optionList.filter { option -> option.isSelected }))
                    }
                    basketListViewModel.add(
                        it.shopId,
                        it.shopName,
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id,
                        0,
                        0,
                        it.id,
                        it.status,
                        it.name,
                        it.isAdultMenu,
                        menuCostListItem.cost,
                        menuCostListItem.saleCost,
                        countTextView.text.toString().toInt(),
                        it.imageUrlList.firstOrNull() ?: "",
                        optionGroupList,
                        menuCostListItem.id,
                        menuCostListItem.name
                    )
                    view.showMessageBar("장바구니에 상품을 담았습니다.")
                    dismiss()
                }
                basketCount = menuCount
            }
        }
    }

    //다이얼로그 크기만큼 나오기
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
        return dialog
    }
}