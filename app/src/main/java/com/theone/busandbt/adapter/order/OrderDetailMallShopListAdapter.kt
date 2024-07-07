package com.theone.busandbt.adapter.order

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.CourierType
import com.busandbt.code.DeliveryType
import com.busandbt.code.ParcelStatus
import com.busandbt.code.ServiceType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.databinding.ItemShoppingOrderDetailBinding
import com.theone.busandbt.dto.order.MallOrderShopInDetail
import com.theone.busandbt.eventbus.MallOrderCancelButtonEvent
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.fragment.menu.MallMenuDetailsFragmentArgs
import com.theone.busandbt.fragment.review.ReviewWriteFragmentArgs
import com.theone.busandbt.fragment.shop.ShoppingDetailFragmentArgs
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.greenrobot.eventbus.EventBus
import org.koin.java.KoinJavaComponent

class OrderDetailMallShopListAdapter(
    orderShopList: List<MallOrderShopInDetail>,
    private val orderId: String,
    private val fragmentManager: FragmentManager,
    private val token: String,
) :
    DataBindingListAdapter<ItemShoppingOrderDetailBinding, MallOrderShopInDetail>() {
    override val viewHolderLayoutId: Int = R.layout.item_shopping_order_detail
    private val orderListAdapter =
        HashMap<Int, OrderDetailMallMenuListAdapter>()
    private val orderAPI: OrderAPI by KoinJavaComponent.inject(OrderAPI::class.java)

    init {
        _items.addAll(orderShopList)
    }

    override fun doBind(
        binding: ItemShoppingOrderDetailBinding,
        item: MallOrderShopInDetail,
        position: Int,
        payloads: MutableList<Any>
    ) {
        with(binding) {
            orderShop = item
            orderCancelButton.isVisible = item.parcelStatus == ParcelStatus.SUSPEND_RECEIPT.id
            shopNameTextView.setOnClickListener {
                it.navigate(
                    R.id.shoppingdetails_graph,
                    ShoppingDetailFragmentArgs(
                        item.shopId,
                        ServiceType.SHOPPING_MALL.id,
                        DeliveryType.PARCEL.id
                    ).toBundle()
                )
            }

            orderCancelButton.setOnClickListener {
                EventBus.getDefault().post(MallOrderCancelButtonEvent(token,orderId,item,binding))
            }

            orderItemRecyclerView.addItemDecoration(
                HorizontalSpaceItemDecoration(
                    SizeUtils.dp2px(
                        24f
                    )
                )
            )
            orderItemRecyclerView.adapter =
                orderListAdapter.getOrPut(item.shopId) {
                    OrderDetailMallMenuListAdapter(item.menuList)
                }.apply {
                    setOnItemClick { view, _, i ->
                        view.navigate(
                            R.id.menu_detail_graph,
                            MallMenuDetailsFragmentArgs(
                                i.menuId,
                                item.shopId,
                                item.shopName,
                                ServiceType.SHOPPING_MALL.id,
                                DeliveryType.PARCEL.id,
                                0
                            ).toBundle()
                        )
                    }
                }
            try {
                if (item.courierType != null && item.waybillNo != null) productNumberTextView.text =
                    "${CourierType.find(item.courierType).desc()} ${item.waybillNo}"
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            copyTextView.setOnClickListener {
                val waybillNo = item.waybillNo ?: return@setOnClickListener
                ClipboardUtils.copyText(waybillNo)
                it?.showMessageBar("복사가 완료되었어요.")
            }
            deliverySituationTextView.text = ParcelStatus.find(item.parcelStatus).desc()
            if (item.isWriteableReview()) {
                reviewWriteInclude.root.isVisible = true
                reviewWriteInclude.reviewWriteButton.setOnClickListener {
                    it.navigate(
                        R.id.review_write_graph,
                        ReviewWriteFragmentArgs(orderId).toBundle()
                    )
                }
            }

            callButton.setOnClickListener {
                fragmentManager.showMessageDialog(
                    "매장으로 전화를 연결합니다.",
                    showWarningImageView = false,
                    showCancelButton = false
                ) {
                    onDoneButtonClick(buttonText = "전화 연결") {
                        PhoneUtils.dial(item.shopTel)
                        dismiss()
                    }
                }
            }
        }
    }

    fun writtenReviewAllOrder() {
        items.forEach { it.doWrittenReview = true }
        notifyItemRangeChanged(0, items.size)
    }
}