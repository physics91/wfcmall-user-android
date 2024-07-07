package com.theone.busandbt.fragment.basket

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.FragmentBasketFoodDeliveryBinding
import com.theone.busandbt.eventbus.basket.ChangeBasketTabEvent
import com.theone.busandbt.extension.*
import com.theone.busandbt.adapter.basket.BasketShopListAdapter
import com.theone.busandbt.fragment.shop.ServiceMainFragmentArgs
import com.theone.busandbt.item.basket.BasketShop
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import com.busandbt.code.DeliveryType
import com.busandbt.code.ServiceType
import org.greenrobot.eventbus.EventBus

/**
 * 장바구니 주문 유형 탭에 따라 갈리는 화면
 */
class BasketDeliveryTypeFragment : BaseBasketFragment<FragmentBasketFoodDeliveryBinding>() {
    override val layoutId: Int = R.layout.fragment_basket_food_delivery
    private val serviceType: ServiceType by lazy { arguments.getServiceType() }
    private val deliveryType: DeliveryType by lazy { arguments.getDeliveryType() }
    private val independentSelectedBasketShop: BasketShop?
        get() = basketListViewModel.getSelectedBasketShop(
            serviceType,
            deliveryType
        )

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().post(
            ChangeBasketTabEvent(
                serviceType, deliveryType,
                independentSelectedBasketShop
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val adapter = BasketShopListAdapter(
                deliveryType,
                basketListViewModel.basketInfoMap ?: return
            ).apply { setHasStableIds(true) }
            (shoppingBasketListRecyclerView.itemAnimator as? SimpleItemAnimator)?.changeDuration = 0
            shoppingBasketListRecyclerView.adapter = adapter
            shoppingBasketListRecyclerView.addItemDecoration(
                HorizontalSpaceItemDecoration(
                    SizeUtils.dp2px(
                        13F
                    )
                )
            )
            val a = activity ?: return
            basketListViewModel.observe(a) {
                val list =
                    it.filter { shop -> shop.serviceTypeId == serviceType.id && shop.deliveryTypeId == deliveryType.id }
                adapter.setItems(list)
                var totalCount = 0
                list.forEach { shop ->
                    totalCount += shop.menuList.size
                }
                notExistBasketInclude.root.isVisible = list.isEmpty()  // 아이템이 없을 때 장바구니가 비었어요 화면 띄움
            }

            allDeleteButton.setOnClickListener {
                showMessageDialog(
                    "장바구니에서 모두 삭제하시겠어요?",
                    showCancelButton = true
                ) {
                    onDoneButtonClick {
                        dismiss()
                        if (adapter.items.isEmpty()) return@onDoneButtonClick
                        basketListViewModel.removeAll(serviceType, deliveryType)
                        adapter.clear()
                        requireContext().showMessageBar("전체 삭제되었어요.")
                    }
                }
            }

            when (serviceType) {
                ServiceType.FOOD_DELIVERY -> {
                    when (deliveryType) {
                        DeliveryType.INSTANT -> chanceDeliveryTypeButton.text = "포장 이동"
                        DeliveryType.PACKAGING -> chanceDeliveryTypeButton.text = "배달 이동"
                        else -> {
                            chanceDeliveryTypeButton.isVisible = false
                            centerLine.isVisible = false
                        }
                    }
                }
                else -> {
                    chanceDeliveryTypeButton.isVisible = false
                    centerLine.isVisible = false
                }
            }
            chanceDeliveryTypeButton.setOnClickListener {
                val basketShop = independentSelectedBasketShop ?: return@setOnClickListener
                val basketInfoMap = basketListViewModel.basketInfoMap ?: return@setOnClickListener
                val basketInfo = basketInfoMap[basketShop.shopId] ?: return@setOnClickListener
                val to = when (serviceType) {
                    ServiceType.FOOD_DELIVERY -> {
                        when (deliveryType) {
                            DeliveryType.INSTANT -> DeliveryType.PACKAGING
                            DeliveryType.PACKAGING -> DeliveryType.INSTANT
                            else -> return@setOnClickListener
                        }
                    }
                    else -> return@setOnClickListener
                }
                if (!basketInfo.deliveryTypeList.contains(to.id)) {
                    showMessageDialog("${to.desc()}이 불가능한 매장입니다.", showWarningImageView = true)
                    return@setOnClickListener
                }
                basketShop.deliveryTypeId = to.id
                basketListViewModel.set(basketShop, deliveryType)
            }
            notExistBasketInclude.goToPutInMenuTextView.setOnClickListener {
                it.navigate(
                    R.id.service_main_graph,
                    ServiceMainFragmentArgs(serviceType.id, deliveryType.id).toBundle()
                )
            }
        }
    }
}