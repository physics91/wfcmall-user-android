package com.theone.busandbt.fragment.order

import android.os.Bundle
import android.text.SpannedString
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.busandbt.code.CancelType
import com.busandbt.code.ParcelStatus
import com.busandbt.code.PaymentType
import com.theone.busandbt.R
import com.theone.busandbt.adapter.coupon.OrderCouponListAdapter
import com.theone.busandbt.adapter.order.OrderDetailMallShopListAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.databinding.FragmentShoppingOrderDetailBinding
import com.theone.busandbt.dto.order.MallOrderDetail
import com.theone.busandbt.dto.order.request.SetOrderCancelRequest
import com.theone.busandbt.dto.order.request.SetParcelInfo
import com.theone.busandbt.eventbus.MallOrderCancelButtonEvent
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.function.formatPhoneNumber
import com.theone.busandbt.model.order.MallOrderDetailViewModel
import com.theone.busandbt.spanned.TypefaceSpanCompat
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MallOrderDetailFragment : DataBindingFragment<FragmentShoppingOrderDetailBinding>(),
    EnabledGoBackButton, RequiredLogin {
    override val layoutId: Int = R.layout.fragment_shopping_order_detail
    override val actionBarTitle: String = "주문상세"
    private val args by navArgs<OrderDetailFragmentArgs>()
    private val orderAPI: OrderAPI by inject()
    private val viewModel: MallOrderDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            viewModel.mallOrderDetailLiveData.observe(viewLifecycleOwner) {
                if (it == null) return@observe
                initView(it)
            }
            if (viewModel.mallOrderDetailLiveData.value == null) {
                root.alpha = 0f
                val innerLoginInfo = loginInfo ?: return
                safeApiRequest(
                    orderAPI.getMallOrderDetail(
                        innerLoginInfo.getFormedToken(),
                        args.orderId
                    )
                ) {
                    viewModel.setMallOrderDetail(it)
                }
            }
        }
    }

    override fun onResultDataReceived(resultData: Bundle) {
        with(binding) {
            if (!resultData.getBoolean("doWrittenReview", false)) return@with
            orderRecyclerView.post {
                val adapter =
                    orderRecyclerView.adapter as? OrderDetailMallShopListAdapter ?: return@post
                adapter.writtenReviewAllOrder()
            }
        }
    }

    private fun initView(od: MallOrderDetail) {
        with(binding) {
            val innerLoginInfo = loginInfo ?: return
            orderRecyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(12F)))
            footerSubTextView.text = buildSpannedString {
                val ctx = requireContext()
                append(
                    "평일",
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(ctx, R.font.sult_bold) ?: return@with
                    ),
                    SpannedString.SPAN_EXCLUSIVE_INCLUSIVE
                )
                append(" AM 9:00 ~ PM 10:00\n")
                append(
                    "주말·공휴일", TypefaceSpanCompat(
                        ResourcesCompat.getFont(ctx, R.font.sult_bold) ?: return@with
                    ),
                    SpannedString.SPAN_EXCLUSIVE_INCLUSIVE
                )
                append(" AM 11:00 ~ PM 10:00")
            }

            callButton.setOnClickListener {
                showMessageDialog(
                    "동백통 고객센터로\n" +
                            "전화를 연결합니다.", getString(R.string.contactUsDesc),
                    showWarningImageView = false,
                    showCancelButton = true
                ) {
                    onDoneButtonClick(buttonText = "전화 연결") {
                        PhoneUtils.dial(StringUtils.getString(R.string.callCenterNo))
                        dismiss()
                    }
                }
            }

            removeOrderButton.setOnClickListener { // 주문 내역 삭제
                showMessageDialog(
                    "주문 내역을 삭제하시겠어요?", "삭제 후에는 복구하실 수 없습니다.",
                    showWarningImageView = false,
                    showCancelButton = true
                ) {
                    onDoneButtonClick {
                        safeApiRequest(
                            orderAPI.removeOrder(innerLoginInfo.getFormedToken(), args.orderId)
                        ) {
                            dismiss()
                            view?.showMessageBar("주문내역 삭제가 완료되었어요.")
                            findNavController().popBackStack()
                        }
                    }
                }
            }
            this.orderDetail = od
            val menuList = od.orderShopList.flatMap { orderShop -> orderShop.menuList }
            if (menuList.isNotEmpty()) {
                if (menuList.size > 1) orderProductTextView.text =
                    menuList[0].menuName + "외 2개"
                else orderProductTextView.text = menuList[0].menuName
            }
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")
            orderDateTextView.text = LocalDateTime.parse(od.createDateTime, inputFormatter)
                .format(outputFormatter)

            orderRecyclerView.adapter = OrderDetailMallShopListAdapter(
                od.orderShopList,
                args.orderId,
                childFragmentManager,
                innerLoginInfo.getFormedToken()
            )
            with(orderInfoForm) {
                orderIdTextView.text = args.orderId
                deliveryAddressTextView.text =
                    "${od.jibun ?: ""}${od.addressDetail ?: ""}\n(도로명)${od.road ?: ""}${od.addressDetail ?: ""}"

                customerTelTitle.text = "수령인"  //
                customerTelTextView.text = "테스트" // 수령인 이름을 넣어줘야함
                shopMemoTitle.text = "연락처"
                shopMemoTextView.text = buildString {
                    if (od.securityTel.isNotEmpty()) append("${formatPhoneNumber(od.securityTel)} (안심번호)\n")
                    append(formatPhoneNumber(od.customerTel))
                }
                riderMemoTitle.text = "배송메모"
                riderMemoTextView.text = od.riderMemo
            }
            with(paymentFormInclude) {
                orderCostTextView.text = od.menuCost.toCommonMoneyForm()
                deliveryCostTextView.text =
                    (od.deliveryCost + od.extraCost).toCommonMoneyForm()
//                        couponDiscountCostTextView.text =
//                            ((it.shopDiscountCost + it.eventDiscountCost)).toMinusCommonMoneyForm()
                packagingDiscountCostTitle.isVisible = false
                packagingDiscountCostTextView.isVisible = false
                deliveryCostTitle.text = "배송비"
                couponDiscountCostTextView.text = "(-)${
                    od.useCouponList.sumOf { oc -> oc.couponDiscountCost }
                        .toCommonMoneyForm()
                }"
                useCouponRecyclerView.adapter = OrderCouponListAdapter(od.useCouponList)
            }
            totalPaymentCostTextView.text = od.paymentCost.toCommonMoneyForm()
            paymentCostTextView.text = od.paymentCost.toCommonMoneyForm()
            paymentNameTextView.text = PaymentType.find(od.paymentType).desc()
            root.playAlphaAnimation()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOrderCancelButtonClicked(event: MallOrderCancelButtonEvent) {
        val innerLoginInfo = loginInfo ?: return
        with(event) {
            showMessageDialog(
                "주문을 취소하시겠어요?",
                showCancelButton = true,
                cancelButtonText = "아니오"
            ) {
                onDoneButtonClick(buttonText = "취소 할래요") {
                    safeApiRequest(
                        orderAPI.setOrderCancel(
                            token, orderId, SetOrderCancelRequest(
                                setParcelInfoList = listOf(
                                    SetParcelInfo(
                                        item.sequence,
                                        parcelStatus = ParcelStatus.CANCEL.id,
                                        cancelType = CancelType.CUSTOMER_REQUEST.id
                                    )
                                ),
                                cancelType = null,
                                status = null
                            )
                        ),
                        showFailMessage = true
                    ) {
                        dismiss()
                        showMessageBar("주문취소가 완료되었어요.")
                        binding.orderCancelButton.isVisible = false
                        binding.deliverySituationTextView.text = "배송취소"
                    }
                }
            }
        }
    }
}