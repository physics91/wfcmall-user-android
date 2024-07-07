package com.theone.busandbt.fragment.order

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.OrderStatus
import com.naver.maps.geometry.Coord
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.databinding.FragmentOrderStatusBinding
import com.theone.busandbt.dto.order.request.SetOrderCancelRequest
import com.theone.busandbt.eventbus.ChangeOrderStatusEvent
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 바로배달 주문현황 프래그먼트
 */
class OrderStatusFragment : DataBindingFragment<FragmentOrderStatusBinding>(), EnabledGoBackButton,
    RequiredLogin, OnMapReadyCallback, EventBusSubscriber {

    companion object {
        private val ORDER_STATUS_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }

    override val layoutId: Int = R.layout.fragment_order_status
    override val actionBarTitle: String = "주문현황"
    private val args by navArgs<OrderStatusFragmentArgs>()
    private val orderAPI: OrderAPI by inject()
    private lateinit var coord: Coord
    private val mapView: MapView get() = binding.packagingFooterForm.shopMap
    private var naverMap: NaverMap? = null

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@OrderStatusFragment)
            safeApiRequest(
                orderAPI.getOrderStatusInfo(innerLoginInfo.getFormedToken(), args.orderId)
            ) {
                orderId = args.orderId
                orderStatusInfo = it
                formByDeliveryType(
                    DeliveryType.find(it.deliveryType),
                    OrderStatus.find(it.status)
                )
                goOrderDetailButton.setOnClickListener {
                    val action =
                        OrderStatusFragmentDirections.actionOrderStatusDirectDeliveryFragmentToOrderDirectDeliveryDetail(
                            args.orderId
                        )
                    findNavController().navigate(action)
                }
                root.playAlphaAnimation()
            }
        }
    }

    override fun onMapReady(mapImg: NaverMap) {
        naverMap = mapImg
        val od = binding.orderStatusInfo ?: return
        formByDeliveryType(DeliveryType.find(od.deliveryType), OrderStatus.find(od.status))
        binding.goOrderDetailButton.setOnClickListener {
            val action =
                OrderStatusFragmentDirections.actionOrderStatusDirectDeliveryFragmentToOrderDirectDeliveryDetail(
                    args.orderId
                )
            findNavController().navigate(action)
        }
        coord = LatLng(od.shopLat ?: 0.0, od.shopLng ?: 0.0)
        val cameraUpdate = CameraUpdate.scrollTo(
            LatLng(
                (coord as LatLng).latitude + 0.000135,
                (coord as LatLng).longitude
            )
        )
        //카메라 위치,줌 설정
        mapImg.moveCamera(cameraUpdate)
        val marker = Marker()
        val uiSettings = mapImg.uiSettings
        mapImg.minZoom = 17.0
        mapImg.maxZoom = 17.0
        //지도상에 마커 표시
        marker.position = coord as LatLng
        marker.map = mapImg
        marker.icon = OverlayImage.fromResource(R.drawable.ic_map_marker)
        marker.width = SizeUtils.dp2px(40f)
        marker.height = SizeUtils.dp2px(60f)
        //UI 컨트롤 재배치
        uiSettings.isCompassEnabled = false
        uiSettings.isScaleBarEnabled = false
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = false
    }


    private fun showCancelOrderPopup() {
        val innerLoginInfo = loginInfo ?: return
        showMessageDialog(
            "주문을 취소하시겠어요?",
            showCancelButton = true,
            cancelButtonText = "아니오"
        ) {
            onDoneButtonClick(buttonText = "취소 할래요") {
                safeApiRequest(
                    orderAPI.setOrderCancel(
                        innerLoginInfo.getFormedToken(),
                        args.orderId,
                        SetOrderCancelRequest()
                    ),
                    showFailMessage = true
                ) {
                    dismiss()
                    showMessageBar("주문취소가 완료되었어요.")
                    this@OrderStatusFragment.findNavController().popBackStack()
                }
            }
        }
    }

    private fun formByDeliveryType(deliveryType: DeliveryType, orderStatus: OrderStatus) {
        with(binding) {
            val od = orderStatusInfo ?: return
            when (deliveryType) {
                DeliveryType.INSTANT, DeliveryType.BUNDLE -> {
                    instantFooterForm.deliveryAddressTextView.text =
                        "${od.jibun ?: ""} ${od.addressDetail ?: ""}\n(도로명)${od.road ?: ""} ${od.addressDetail ?: ""}"
                    instantFooterForm.customerTelTextView.text = od.customerTel
                    instantFooterForm.shopMemoTextView.text = od.shopMemo
                    instantFooterForm.riderMemoTextView.text = od.riderMemo
                    instantForm.receiptTimeTextView.text =
                        od.createDateTime.toLocalDateTime().toOrderStatusTime()
                    instantForm.receiptTimeTextView.isVisible = true
                    instantForm.receiptTextView.isSelected = true
                    when (orderStatus) {
                        OrderStatus.NEW_OR_RECEIVING -> {
                            instantForm.orderStatusTextView.text = "주문을 확인하고 있어요."
                            instantForm.receiptCheck.isChecked = true
                            instantForm.orderCancelButton.setOnClickListener {
                                showCancelOrderPopup()
                            }
                        }

                        OrderStatus.START_COOKING -> {
                            instantForm.cookingTextView.isSelected = true
                            val acceptTime = od.acceptDateTime
                            if (acceptTime != null) {
                                val aTime = acceptTime.toLocalDateTime()
                                    .plusMinutes(od.doneExpectMinutes.toLong()).toOrderStatusTime()
                                instantForm.expectedTime.text = "$aTime 도착예정"
                                instantForm.expectedTime.isVisible = true
                                instantForm.cookingTimeTextView.text =
                                    acceptTime.toLocalDateTime().toOrderStatusTime()
                                instantForm.cookingTimeTextView.isVisible = true
                            }
                            instantForm.orderStatusTextView.text = "매장에서 맛있게 조리 중이에요."
                            instantForm.receiptCheck.isChecked = true
                            instantForm.cookingCheck.isChecked = true
                            instantForm.receipttLine.isSelected = true
                            instantForm.orderCancelButton.setOnClickListener(null)
                            instantForm.orderCancelButton.text = "취소불가"
                            instantForm.orderCancelButton.setTextColor(ColorUtils.getColor(R.color.cancelColor))
                        }

                        OrderStatus.START_DELIVERY -> {
                            instantForm.cookingTextView.isSelected = true
                            instantForm.deliveryCheck.isSelected = true
                            val acceptTime = od.acceptDateTime
                            if (acceptTime != null) {
                                val aTime = acceptTime.toLocalDateTime()
                                    .plusMinutes(od.doneExpectMinutes.toLong()).toOrderStatusTime()
                                instantForm.expectedTime.text = "$aTime 도착예정"
                                instantForm.expectedTime.isVisible = true
                                instantForm.cookingTimeTextView.text =
                                    acceptTime.toLocalDateTime().toOrderStatusTime()
                                instantForm.cookingTimeTextView.isVisible = true
                            }
                            instantForm.deliveryTextView.isSelected = true
                            instantForm.deliveryTimeTextView.text =
                                od.deliveryStartDateTime?.toLocalDateTime()?.toOrderStatusTime()
                                    ?: ""
                            instantForm.deliveryTimeTextView.isVisible = true
                            instantForm.orderStatusTextView.text = "주소지로 배달 중이에요."
                            instantForm.receiptCheck.isChecked = true
                            instantForm.cookingCheck.isChecked = true
                            instantForm.deliveryCheck.isChecked = true
                            instantForm.receipttLine.isSelected = true
                            instantForm.cookLine.isSelected = true
                            instantForm.orderCancelButton.setOnClickListener(null)
                            instantForm.orderCancelButton.text = "취소불가"
                            instantForm.orderCancelButton.setTextColor(ColorUtils.getColor(R.color.cancelColor))
                        }

                        OrderStatus.COMPLETE_DELIVERY -> {
                            instantForm.cookingTimeTextView.text =
                                od.acceptDateTime?.toLocalDateTime()?.toOrderStatusTime() ?: ""
                            instantForm.cookingTimeTextView.isVisible = true
                            instantForm.deliveryTextView.isSelected = true
                            instantForm.deliveryTimeTextView.text =
                                od.deliveryStartDateTime?.toLocalDateTime()?.toOrderStatusTime()
                                    ?: ""
                            instantForm.deliveryClearTextView.isSelected = true
                            instantForm.deliveryTimeTextView.isVisible = true
                            instantForm.deliveryClearTimeTextView.text =
                                od.deliveryDoneDateTime?.toLocalDateTime()?.toOrderStatusTime()
                                    ?: ""
                            instantForm.deliveryClearTimeTextView.isVisible = true
                            instantForm.orderStatusTextView.text = "주문을 확인하고 있어요."
                            instantForm.receiptCheck.isChecked = true
                            instantForm.cookingCheck.isChecked = true
                            instantForm.deliveryCheck.isChecked = true
                            instantForm.deliveryClearCheck.isChecked = true
                            instantForm.receipttLine.isSelected = true
                            instantForm.cookLine.isSelected = true
                            instantForm.deliveryLine.isSelected = true
                            instantForm.orderCancelButton.setOnClickListener(null)
                            instantForm.orderCancelButton.text = "취소불가"
                            instantForm.orderCancelButton.setTextColor(ColorUtils.getColor(R.color.cancelColor))
                        }

                        OrderStatus.CANCEL -> {

                        }

                        else -> {}
                    }
                    instantFooterForm.root.isVisible = true
                    instantForm.root.isVisible = true
                }

                DeliveryType.PACKAGING -> {
                    packagingFooterForm.shopAddressTextView.text =
                        "${od.shopJibun ?: ""}${od.shopAddressDetail ?: ""}\n(도로명)${od.shopRoad ?: ""}${od.shopAddressDetail ?: ""}"
                    packagingFooterForm.customerTelTextView.text = od.customerTel
                    packagingFooterForm.shopMemoTextView.text = od.shopMemo
                    packagingFooterForm.shopAddressCopyButton.setOnClickListener {
                        ClipboardUtils.copyText("${od.shopRoad ?: ""}${od.shopAddressDetail ?: ""}")
                        view?.showMessageBar("주소복사가 완료되었어요.")
                    }
                    packagingForm.receiptTime.text =
                        od.createDateTime.toLocalDateTime().toOrderStatusTime()
                    packagingForm.receiptTime.isVisible = true
                    packagingForm.receipt.isSelected = true
                    when (orderStatus) {
                        OrderStatus.NEW_OR_RECEIVING -> {
                            packagingForm.orderStatusTextView.text = "주문을 확인하고 있어요."
                            packagingForm.firstCheck.isChecked = true
                            packagingForm.orderCancelButton.setOnClickListener {
                                showCancelOrderPopup()
                            }
                        }

                        OrderStatus.START_COOKING -> {
                            packagingForm.cooking.isSelected = true
                            val acceptTime = od.acceptDateTime
                            if (acceptTime != null) {
                                val aTime = acceptTime.toLocalDateTime()
                                    .plusMinutes(od.doneExpectMinutes.toLong()).toOrderStatusTime()
                                packagingForm.expectedTime.text = "$aTime 포장완료 예정"
                                packagingForm.expectedTime.isVisible = true
                                packagingForm.cookingTime.text =
                                    acceptTime.toLocalDateTime().toOrderStatusTime()
                                packagingForm.cookingTime.isVisible = true
                            }
                            packagingForm.orderStatusTextView.text = "매장에서 맛있게 조리 중이에요."
                            packagingForm.firstCheck.isChecked = true
                            packagingForm.secondCheck.isChecked = true
                            packagingForm.firstLine.isSelected = true
                            packagingForm.orderCancelButton.setOnClickListener(null)
                            packagingForm.orderCancelButton.text = "취소불가"
                            packagingForm.orderCancelButton.setTextColor(ColorUtils.getColor(R.color.cancelColor))
                        }

                        OrderStatus.COMPLETE_PACKAGING -> {
                            packagingForm.expectedTime.isVisible = false
                            packagingForm.cookingTime.text =
                                od.acceptDateTime?.toLocalDateTime()?.toOrderStatusTime() ?: ""
                            packagingForm.cookingTime.isVisible = true
                            packagingForm.deliveryClear.isSelected = true
                            packagingForm.deliveryClearTime.text =
                                od.deliveryDoneDateTime?.toLocalDateTime()?.toOrderStatusTime()
                                    ?: ""
                            packagingForm.deliveryClearTime.isVisible = true
                            packagingForm.orderStatusTextView.text =
                                "포장 준비가 완료되었어요.\n매장에 직접 방문하여 픽업하세요!"
                            packagingForm.firstCheck.isChecked = true
                            packagingForm.secondCheck.isChecked = true
                            packagingForm.thirdCheck.isChecked = true
                            packagingForm.firstLine.isSelected = true
                            packagingForm.secondLine.isSelected = true
                            packagingForm.orderCancelButton.setOnClickListener(null)
                            packagingForm.orderCancelButton.text = "취소불가"
                            packagingForm.orderCancelButton.setTextColor(ColorUtils.getColor(R.color.cancelColor))
                        }

                        OrderStatus.CANCEL -> {

                        }

                        else -> {}
                    }
                    packagingFooterForm.root.isVisible = true
                    packagingForm.root.isVisible = true
                }

                else -> {}
            }
        }
    }

    private fun LocalDateTime.toOrderStatusTime(): String = ORDER_STATUS_FORMATTER.format(this)

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeOrderStatus(event: ChangeOrderStatusEvent) {
        if (args.orderId != event.orderId) return
        if (event.orderStatus in arrayOf(OrderStatus.CANCEL)) {
            findNavController().popBackStack()
            return
        }
        val od = binding.orderStatusInfo ?: return
        if (event.orderDoneMinutes != null) {
            od.doneExpectMinutes = event.orderDoneMinutes
        }
        val innerLoginInfo = loginInfo ?: return
        safeApiRequest(
            orderAPI.getOrderStatusInfo(innerLoginInfo.getFormedToken(), args.orderId)
        ) {
            binding.orderStatusInfo = it
            formByDeliveryType(DeliveryType.find(it.deliveryType), OrderStatus.find(it.status))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshOrderStatus()
    }

    /**
     * onStop 상태에서 ->  onResume 상태가 되었을 때 상태값을 다시 조회하기 위한 함수.
     */
    private fun refreshOrderStatus() {
        val innerLoginInfo = loginInfo ?: return
        safeApiRequest(
            orderAPI.getOrderStatusInfo(innerLoginInfo.getFormedToken(), args.orderId)
        ) { orderStatusInfo ->
            binding.orderStatusInfo = orderStatusInfo
            formByDeliveryType(
                DeliveryType.find(orderStatusInfo.deliveryType),
                OrderStatus.find(orderStatusInfo.status)
            )
        }
    }
}