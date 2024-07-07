package com.theone.busandbt.fragment.order

import android.os.Bundle
import android.text.SpannedString
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.databinding.FragmentOrderDetailBinding
import com.theone.busandbt.dto.address.Location
import com.theone.busandbt.extension.desc
import com.theone.busandbt.extension.navigate
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.extension.toCommonMoneyForm
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.adapter.coupon.OrderCouponListAdapter
import com.theone.busandbt.adapter.order.OrderDetailMenuListAdapter
import com.theone.busandbt.fragment.address.AddressLocationFragmentArgs
import com.theone.busandbt.fragment.review.ReviewWriteFragmentArgs
import com.theone.busandbt.fragment.shop.ShopDetailFragmentArgs
import com.theone.busandbt.spanned.TypefaceSpanCompat
import com.busandbt.code.DeliveryType
import com.busandbt.code.OrderStatus
import com.busandbt.code.PaymentType
import com.naver.maps.geometry.Coord
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.function.formatPhoneNumber
import org.koin.android.ext.android.inject

/**
 * 바로배달 주문상세 프래그먼트
 */
class OrderDetailFragment : DataBindingFragment<FragmentOrderDetailBinding>(), OnMapReadyCallback,
    EnabledGoBackButton,
    RequiredLogin, View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_order_detail
    override val actionBarTitle: String = "주문상세"
    private lateinit var coord: Coord

    private val args by navArgs<OrderDetailFragmentArgs>()
    private val orderAPI: OrderAPI by inject()
    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            mapView = orderDetailMap
            mapView.onCreate(savedInstanceState)
            addressCopyTextView.setOnClickListener(this@OrderDetailFragment)
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
            
            safeApiRequest(
                orderAPI.getOrderDetail(
                    loginInfo?.getFormedToken() ?: return,
                    args.orderId
                )
            ) {
                orderDetail = it
                mapView.getMapAsync(this@OrderDetailFragment)
                deliveryTypeTextView.text = DeliveryType.find(it.deliveryType).desc()
                orderMenuRecyclerView.adapter = OrderDetailMenuListAdapter(it.menuList)
                with(orderInfoForm) {
                    orderIdTextView.text = args.orderId
                    deliveryAddressTextView.text =
                        "${it.jibun ?: ""} ${it.addressDetail ?: ""}\n(도로명)${it.road ?: ""} ${it.addressDetail ?: ""}"
                    customerTelTextView.text = buildString {
                        if (it.securityTel.isNotEmpty()) append("${formatPhoneNumber(it.securityTel)} (안심번호)\n")
                        append(formatPhoneNumber(it.customerTel))
                    }
                    shopMemoTextView.text = it.shopMemo
                    riderMemoTextView.text = it.riderMemo
                    if (it.deliveryType == 2) {
                        deliveryAddressTitle.isVisible = false
                        deliveryAddressTextView.isVisible = false
                        riderMemoTitle.isVisible = false
                        riderMemoTextView.isVisible = false
                    }
                }
                paymentFormInclude.orderDetail = it
                paymentFormInclude.deliveryType = DeliveryType.find(it.deliveryType)
                if (it.deliveryType == DeliveryType.PACKAGING.id) {
                    addressCopyTextView.isVisible = true
                    deliveryTypeTextView.setBackgroundResource(R.drawable.bg_round_9dp_order_detail_packing_form)
                    deliveryTypeTextView.setTextColor(ColorUtils.getColor(R.color.orderDetailPackingText))
                    deliveryTypeTextView.text = "포장"
                    paymentFormInclude.packagingDiscountCostTextView.text =
                        "(-)${it.packagingDiscountCost.toCommonMoneyForm()}"
                } else {
                    orderDetailMapCardView.isVisible = false
                    deliveryTypeTextView.setBackgroundResource(R.drawable.bg_market_order_text)
                    deliveryTypeTextView.setTextColor(ColorUtils.getColor(R.color.mainColor))
                    deliveryTypeTextView.text = "배달"
                    paymentFormInclude.packagingDiscountCostTitle.isVisible = false
                    paymentFormInclude.packagingDiscountCostTextView.isVisible = false
                }
                with(paymentFormInclude) {
                    orderCostTextView.text = it.menuCost.toCommonMoneyForm()
                    deliveryCostTextView.text =
                        (it.deliveryCost + it.extraCost).toCommonMoneyForm()
                    couponDiscountCostTextView.text = "(-)${
                        it.useCouponList.sumOf { oc -> oc.couponDiscountCost }
                            .toCommonMoneyForm()
                    }"
                    useCouponRecyclerView.adapter = OrderCouponListAdapter(it.useCouponList)
                }

                totalPaymentCostTextView.text = it.paymentCost.toCommonMoneyForm()
                paymentCostTextView.text = it.paymentCost.toCommonMoneyForm()
                paymentNameTextView.text = PaymentType.find(it.paymentType).desc()

                if (it.isWriteableReview()) {
                    reviewWriteInclude.root.isVisible = true
                    reviewWriteInclude.reviewWriteButton.setOnClickListener(this@OrderDetailFragment)
                }

                callButton.setOnClickListener(this@OrderDetailFragment)
                orderDetailMenuInclude.callShopButton.setOnClickListener(this@OrderDetailFragment)
                when (OrderStatus.find(it.status)) {
                    OrderStatus.COMPLETE_PICK_UP, OrderStatus.COMPLETE_DELIVERY -> {
                        removeOrderButton.isVisible = true
                        removeOrderButton.setOnClickListener(this@OrderDetailFragment)
                    }

                    else -> {}
                }
                orderDetailMenuInclude.goShopDetailButton.setOnClickListener(this@OrderDetailFragment)
                shopLocationMapExpandButton.setOnClickListener { _ ->
                    findNavController().navigate(
                        R.id.address_location_graph,
                        AddressLocationFragmentArgs(
                            Location(
                                it.shopJibun ?: return@setOnClickListener,
                                it.shopRoad ?: return@setOnClickListener,
                                it.shopLat ?: return@setOnClickListener,
                                it.shopLng ?: return@setOnClickListener
                            )
                        ).toBundle()
                    )
                }
            }
        }
    }

    override fun onResultDataReceived(resultData: Bundle) {
        with(binding) {
            val doWrittenReview = resultData.getBoolean("doWrittenReview", false)
            if (doWrittenReview) reviewWriteInclude.root.isVisible = false
        }
    }

    override fun onClick(v: View?) {
        with(binding) {
            val od = orderDetail ?: return
            when (v) {
                addressCopyTextView -> {
                    ClipboardUtils.copyText("${od.shopRoad ?: ""}${od.shopAddressDetail ?: ""}")
                    view?.showMessageBar("주소 복사가 완료되었습니다.")
                }

                orderDetailMenuInclude.callShopButton -> PhoneUtils.dial(od.shopTel) // 매장에게 전화
                callButton -> {
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
                } // 고객센터
                removeOrderButton -> { // 주문 내역 삭제
                    showMessageDialog(
                        "주문 내역을 삭제하시겠어요?", "삭제 후에는 복구하실 수 없습니다.",
                        showWarningImageView = false,
                        showCancelButton = true
                    ) {
                        onDoneButtonClick {
                            val innerLoginInfo = loginInfo ?: return@onDoneButtonClick
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
                // 리뷰 작성하기
                reviewWriteInclude.reviewWriteButton -> reviewWriteInclude.reviewWriteButton.navigate(
                    R.id.review_write_graph,
                    ReviewWriteFragmentArgs(args.orderId).toBundle()
                )
                // 매장 상세화면 보기
                orderDetailMenuInclude.goShopDetailButton -> orderDetailMenuInclude.goShopDetailButton.navigate(
                    R.id.shop_detail_graph,
                    ShopDetailFragmentArgs(
                        od.shopId,
                        od.serviceType,
                        od.deliveryType
                    ).toBundle()
                )

                else -> {}
            }
        }
    }

    override fun onMapReady(mapImg: NaverMap) {
        val uiSettings = mapImg.uiSettings
        //UI 컨트롤 재배치
        uiSettings.isCompassEnabled = false
        uiSettings.isScaleBarEnabled = false
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = false
        val sd = binding.orderDetail ?: return
        coord = LatLng(sd.shopLat ?: return, sd.shopLng ?: return)
        val cameraUpdate = CameraUpdate.scrollTo(
            LatLng(
                (coord as LatLng).latitude + 0.000135,
                (coord as LatLng).longitude
            )
        )
        val marker = Marker()
        //카메라 위치,줌 설정
        mapImg.moveCamera(cameraUpdate)
        mapImg.minZoom = 17.0
        mapImg.maxZoom = 17.0
        //지도상에 마커 표시
        marker.position = coord as LatLng
        marker.map = mapImg
        marker.icon = OverlayImage.fromResource(R.drawable.ic_map_marker)
        marker.width = SizeUtils.dp2px(40f)
        marker.height = SizeUtils.dp2px(60f)
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
}