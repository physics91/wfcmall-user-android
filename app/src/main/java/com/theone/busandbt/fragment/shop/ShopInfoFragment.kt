package com.theone.busandbt.fragment.shop

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.SizeUtils
import com.busandbt.code.DeliveryType
import com.busandbt.code.OperTimeType
import com.busandbt.code.Period
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.theone.busandbt.R
import com.theone.busandbt.adapter.shop.OperListAdapter
import com.theone.busandbt.adapter.shop.ShopInfoDeliveryCostAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.orderchannel.ShopAPI
import com.theone.busandbt.bindingadapter.CommonBindingAdapter
import com.theone.busandbt.databinding.FragmentShopInfoBinding
import com.theone.busandbt.dialog.MenuImageDialog
import com.theone.busandbt.dto.address.Location
import com.theone.busandbt.dto.shop.ShopInfo
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.item.ShopInfoCost
import com.theone.busandbt.item.ShopInfoDeliveryCost
import com.theone.busandbt.item.shop.OperViewMetadata
import com.theone.busandbt.type.OperViewType
import com.theone.busandbt.utils.SHOP_NOTICE_COLLAPSING_LINES
import com.theone.busandbt.view.recyclerview.decoration.HorizontalSpaceItemDecoration
import org.koin.android.ext.android.inject
import java.time.DayOfWeek

/**
 * 매장정보 페이지
 */
class ShopInfoFragment : DataBindingFragment<FragmentShopInfoBinding>(), OnMapReadyCallback,
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_shop_info
    override val actionBarTitle: String = "매장 정보"
    private val args by navArgs<ShopInfoFragmentArgs>()
    private val shopAPI: ShopAPI by inject()
    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            mapView = shopLocationMap
            mapView.clipToOutline = true
            mapView.onCreate(savedInstanceState)
            safeApiRequest(
                shopAPI.getShopInfo(args.shopId)
            ) {
                shopInfo = it

                //원산지 없을 때
                if (it.cooInfo.isEmpty()) {
                    cooInfoTextView.isVisible = false
                    cooInfoTitle.isVisible = false
                    cooInfoUpLine.isVisible = false
                }

                val imageUrlList = it.imageUrlList
                if (imageUrlList.isEmpty() && it.notice.isEmpty())
                    shopInfoHeaderForm.isVisible = false
                else {
                    if (imageUrlList.isNotEmpty()) shopImageGroup.isVisible = true
                    else {
                        shopNoticeGroup.isVisible = true
                        shopNoticeTextView.maxLines = SHOP_NOTICE_COLLAPSING_LINES
                    }
                }
                imageUrlList.forEachIndexed { index, s ->
                    if (index > 2) return@forEachIndexed
                    when (index) {
                        0 -> {
                            firstShopImageView.isVisible = true
                            CommonBindingAdapter.glideImageUrl(firstShopImageView, s)
                        }

                        1 -> {
                            secondShopImageView.isVisible = true
                            CommonBindingAdapter.glideImageUrl(secondShopImageView, s)
                        }

                        2 -> {
                            thirdShopImageView.isVisible = true
                            CommonBindingAdapter.glideImageUrl(thirdShopImageView, s)
                        }
                    }
                }

                //지도 객체 생성
                // getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
                // onMapReady에서 NaverMap 객체를 받음
                mapView.getMapAsync(this@ShopInfoFragment)

                firstShopImageView.clipToOutline = true
                secondShopImageView.clipToOutline = true
                thirdShopImageView.clipToOutline = true

                callToShopButton.isVisible = it.tel.isNotEmpty()

                val shopInfoDeliveryCostList = ArrayList<ShopInfoDeliveryCost>()
                val deliveryCostInfoList = it.deliveryCostList.mapIndexed { index, deliveryCost ->
                    val isLastElement = index == it.deliveryCostList.size - 1
                    val label = when {
                        index == 0 && !isLastElement -> "${deliveryCost.testOrderCost.toCommonMoneyForm()} 이상 ~ ${it.deliveryCostList[index + 1].testOrderCost.toCommonMoneyForm()} 미만"
                        isLastElement -> "${deliveryCost.testOrderCost.toCommonMoneyForm()} 이상"
                        else -> "${deliveryCost.testOrderCost.toCommonMoneyForm()} 이상 ~ ${it.deliveryCostList[index + 1].testOrderCost.toCommonMoneyForm()} 미만"
                    }
                    ShopInfoCost(label, deliveryCost.cost)
                }
                if (deliveryCostInfoList.isNotEmpty()) shopInfoDeliveryCostList.add(
                    ShopInfoDeliveryCost("기본 배달비", deliveryCostInfoList)
                )

                val baseDistanceExtraList = it.distanceExtraList.sortedByDescending { distanceExtra -> distanceExtra.testDistance }
                val distanceExtraList =
                    baseDistanceExtraList
                        .mapIndexed { index, distanceExtra ->
                            val minDistance = when (index) {
                                0 -> -1.0
                                else -> baseDistanceExtraList[index - 1].testDistance
                            }
                            val label =
                                if (minDistance == -1.0) "${distanceExtra.testDistance.formatDistance()} 초과" else "${distanceExtra.testDistance.formatDistance()} ~ ${minDistance.formatDistance()}"
                            ShopInfoCost(label, distanceExtra.cost)
                        }
                if (distanceExtraList.isNotEmpty()) shopInfoDeliveryCostList.add(
                    ShopInfoDeliveryCost(
                        "거리별 추가 배달비",
                        distanceExtraList.reversed()
                    )
                )
                val dongExtraList = it.dongExtraList.groupBy({ dongExtra -> dongExtra.cost },
                    { dongExtra -> dongExtra.adminAreaName })
                    .map { t -> ShopInfoCost(t.value.joinToString(), t.key) }
                if (dongExtraList.isNotEmpty()) shopInfoDeliveryCostList.add(
                    ShopInfoDeliveryCost(
                        "지역별 추가 배달비",
                        dongExtraList
                    )
                )

                val timeExtraList =
                    it.timeExtraList.groupBy { te -> "${te.startTime}${te.endTime}${te.cost}" }
                        .map { (_, u) ->
                            val sorted = u.sortedBy { te -> te.dayOfWeek }
                            if (sorted.size == 1) {
                                val timeExtra = sorted.first()
                                val label =
                                    "[${timeExtra.dayOfWeek.toDayOfWeekShort()}] ${timeExtra.startTime.toHourMinuteString()} ~ ${timeExtra.endTime.toHourMinuteString()}"
                                return@map ShopInfoCost(label, timeExtra.cost)
                            }
                            val isConsecutive = sorted.map { te -> te.dayOfWeek }.isConsecutive()
                            if (isConsecutive) {
                                if (sorted.size == 2) {
                                    val timeExtra = sorted.first()
                                    val dayOfWeekString =
                                        sorted.joinToString { te -> te.dayOfWeek.toDayOfWeekShort() }
                                    val label =
                                        "[$dayOfWeekString] ${timeExtra.startTime.toHourMinuteString()} ~ ${timeExtra.endTime.toHourMinuteString()}"
                                    return@map ShopInfoCost(label, timeExtra.cost)
                                } else {
                                    val first = sorted.first()
                                    val last = sorted.last()
                                    val label =
                                        "[${first.dayOfWeek.toDayOfWeekShort()}-${last.dayOfWeek.toDayOfWeekShort()}] ${first.startTime.toHourMinuteString()} ~ ${first.endTime.toHourMinuteString()}"
                                    return@map ShopInfoCost(label, first.cost)
                                }
                            } else {
                                val timeExtra = sorted.first()
                                val dayOfWeekString =
                                    sorted.joinToString { te -> te.dayOfWeek.toDayOfWeekShort() }
                                val label =
                                    "[$dayOfWeekString] ${timeExtra.startTime.toHourMinuteString()} ~ ${timeExtra.endTime.toHourMinuteString()}"
                                return@map ShopInfoCost(label, timeExtra.cost)
                            }
                        }
                if (timeExtraList.isNotEmpty()) shopInfoDeliveryCostList.add(
                    ShopInfoDeliveryCost(
                        "시간대별 추가 배달비",
                        timeExtraList
                    )
                )
                deliveryCostRecyclerView.adapter =
                    ShopInfoDeliveryCostAdapter(shopInfoDeliveryCostList, it.deliveryType)

                if (args.goDeliveryCostInfo) {
                    nestedScrollView.post {
                        nestedScrollView.scrollToView(deliveryCostInfoTitle)
                    }
                }

                // TODO - 휴무일 할증 디자인이랑 데이터랑 상이
                initOperationTime(it)

                shopImageGroup.setOnClickListener { _ ->
                    if (it.imageUrlList.isEmpty()) return@setOnClickListener
                    val dialog = MenuImageDialog()
                    dialog.arguments = bundleOf("imageUrlList" to it.imageUrlList.toTypedArray())
                    dialog.show(
                        childFragmentManager, null
                    )
                }

                shopNoticeExpandTouchArea.setOnClickListener { _ ->
                    if (it.imageUrlList.isNotEmpty()) shopNoticeGroup.isVisible =
                        !shopNoticeGroup.isVisible
                    else {
                        val flag = shopNoticeExpandButton.isSelected
                        shopNoticeTextView.maxLines =
                            if (flag) SHOP_NOTICE_COLLAPSING_LINES else Int.MAX_VALUE
                    }
                    shopNoticeExpandButton.isSelected = !shopNoticeExpandButton.isSelected
                }
                shopLocationMapExpandButton.setOnClickListener { _ ->
                    val action =
                        ShopInfoFragmentDirections.actionStoreInformationFragmentToFullAddress(
                            Location(it.jibun, it.road, it.lat, it.lng),
                            false
                        )
                    findNavController().navigate(action)
                }
                callToShopButton.setOnClickListener { _ ->
                    showMessageDialog("매장으로 전화를 연결합니다.") {
                        onDoneButtonClick(buttonText = "전화연결") {
                            dismiss()
                            PhoneUtils.dial(it.tel)
                        }
                    }
                }
                val isMall =
                    it.deliveryType.any { d -> d == DeliveryType.PARCEL }
                if (isMall) {
                    shoppingGroup.isVisible = false
                    deliveryguideGroup.isVisible = false
                    openTimeGroup.isVisible = false
                }
                //매장소개 없을 때
                if (it.intro.isEmpty()) {
                    shopIntroTextView.isVisible = false
                    shopIntroTitle.isVisible = false
                    shopIntroUnderLine.isVisible = false
                    shopIntroUnderLine.isVisible = false
                }
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        val shopInfo = binding.shopInfo ?: return
        //좌표값과 좌표 범위값 설정
        val shopLocation = LatLng(shopInfo.lat, shopInfo.lng)
        val coordRange =
            LatLng(shopLocation.latitude + 0.0000001, shopLocation.longitude + 0.0000001)
        val cameraUpdate = CameraUpdate.scrollTo(shopLocation)
        val marker = Marker()
        val uiSettings = naverMap.uiSettings
        //지도 구현시 초기 위치 및 표시 범위 설정
        naverMap.moveCamera(cameraUpdate)
        naverMap.extent = LatLngBounds(shopLocation, coordRange)
        //지도의 최대 최소 줌 설정. 해당 프래그먼트에서는 크기를 고정하였다.
        naverMap.minZoom = 17.0
        naverMap.maxZoom = 17.0
        //지도상에 마커 표시
        marker.position = shopLocation
        marker.map = naverMap
        marker.icon = OverlayImage.fromResource(R.drawable.ic_map_marker)
        marker.width = SizeUtils.dp2px(40f)
        marker.height = SizeUtils.dp2px(60f)
        //UI 컨트롤 재배치
        uiSettings.isCompassEnabled = false
        uiSettings.isScaleBarEnabled = false
        uiSettings.isZoomControlEnabled = false
        uiSettings.isLocationButtonEnabled = false
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

    private fun initOperationTime(shopInfo: ShopInfo) {
        with(binding) {
            val operViewMetadataList = ArrayList<OperViewMetadata>()
            val operText = shopInfo.operationTimeList
                .filter { ot -> ot.type == OperTimeType.WORK.id }
                .groupBy { te -> "${te.startTime}${te.endTime}" }
                .map { (_, u) ->
                    val sorted = u.sortedBy { te -> te.dayOfWeek }
                    if (sorted.size == 1) {
                        val timeExtra = sorted.first()
                        return@map "[${
                            DayOfWeek.of(timeExtra.dayOfWeek).desc()
                        }] ${timeExtra.startTime.toKoreanTimeString()} ~ ${timeExtra.endTime.toKoreanTimeString()}"
                    }
                    val isConsecutive = sorted.map { te -> te.dayOfWeek }.isConsecutive()
                    if (isConsecutive) {
                        when (sorted.size) {
                            2 -> {
                                val timeExtra = sorted.first()
                                val dayOfWeekString =
                                    sorted.joinToString { te -> te.dayOfWeek.toDayOfWeekShort() }
                                return@map "[$dayOfWeekString] ${timeExtra.startTime.toKoreanTimeString()} ~ ${timeExtra.endTime.toKoreanTimeString()}"
                            }

                            7 -> {
                                val timeExtra = sorted.first()
                                return@map "[매일] ${timeExtra.startTime.toKoreanTimeString()} ~ ${timeExtra.endTime.toKoreanTimeString()}"
                            }

                            else -> {
                                val first = sorted.first()
                                val last = sorted.last()
                                return@map "[${first.dayOfWeek.toDayOfWeekShort()}-${last.dayOfWeek.toDayOfWeekShort()}] ${first.startTime.toKoreanTimeString()} ~ ${first.endTime.toKoreanTimeString()}"
                            }
                        }
                    } else {
                        val timeExtra = sorted.first()
                        val dayOfWeekString =
                            sorted.joinToString { te -> te.dayOfWeek.toDayOfWeekShort() }
                        return@map "[$dayOfWeekString] ${timeExtra.startTime.toKoreanTimeString()} ~ ${timeExtra.endTime.toKoreanTimeString()}"
                    }
                }.joinToString(separator = "\n")
            val restText = shopInfo.operationTimeList
                .filter { ot -> ot.type == OperTimeType.REST.id }
                .groupBy { te -> "${te.startTime}${te.endTime}" }
                .map { (_, u) ->
                    val sorted = u.sortedBy { te -> te.dayOfWeek }
                    if (sorted.size == 1) {
                        val timeExtra = sorted.first()
                        return@map "[${
                            DayOfWeek.of(timeExtra.dayOfWeek).desc()
                        }] ${timeExtra.startTime.toKoreanTimeString()} ~ ${timeExtra.endTime.toKoreanTimeString()}"
                    }
                    val isConsecutive = sorted.map { te -> te.dayOfWeek }.isConsecutive()
                    if (isConsecutive) {
                        when (sorted.size) {
                            2 -> {
                                val timeExtra = sorted.first()
                                val dayOfWeekString =
                                    sorted.joinToString { te -> te.dayOfWeek.toDayOfWeekShort() }
                                return@map "[$dayOfWeekString] ${timeExtra.startTime.toKoreanTimeString()} ~ ${timeExtra.endTime.toKoreanTimeString()}"
                            }

                            7 -> {
                                val timeExtra = sorted.first()
                                return@map "[매일] ${timeExtra.startTime.toKoreanTimeString()} ~ ${timeExtra.endTime.toKoreanTimeString()}"
                            }

                            else -> {
                                val first = sorted.first()
                                val last = sorted.last()
                                return@map "[${first.dayOfWeek.toDayOfWeekShort()}-${last.dayOfWeek.toDayOfWeekShort()}] ${first.startTime.toKoreanTimeString()} ~ ${first.endTime.toKoreanTimeString()}"
                            }
                        }
                    } else {
                        val timeExtra = sorted.first()
                        val dayOfWeekString =
                            sorted.joinToString { te -> te.dayOfWeek.toDayOfWeekShort() }
                        return@map "[$dayOfWeekString] ${timeExtra.startTime.toKoreanTimeString()} ~ ${timeExtra.endTime.toKoreanTimeString()}"
                    }
                }.joinToString(separator = "\n")
            operViewMetadataList.add(OperViewMetadata(OperViewType.ON_WORK, operText))
            restText.takeIf { it.isNotEmpty() }
                ?.let { operViewMetadataList.add(OperViewMetadata(OperViewType.REST, it)) }
            val routineHolidayText = buildString {
                append(shopInfo.routineHolidayList.joinToString {
                    "${Period.find(it.period).desc()} ${
                        it.dayOfWeek?.let { dw ->
                            DayOfWeek.of(dw).desc()
                        }
                    }"
                })
                shopInfo.legalHolidayNameList.takeIf { it.isNotEmpty() }
                    ?.let { append(", ${it.joinToString()}") }
            }
            operViewMetadataList.add(
                OperViewMetadata(
                    OperViewType.ROUTINE_HOLIDAY,
                    routineHolidayText.ifEmpty { "연중무휴" }
                )
            )

            val actualTempHolidayList =
                shopInfo.tempHolidayList
            if (actualTempHolidayList.isNotEmpty()) {
                val tempHolidayText = actualTempHolidayList.joinToString {
                    if (it.startDate == it.endDate) it.startDate.toKoreanDateString() else "${it.startDate.toKoreanDateString()} ~ ${it.endDate.toKoreanDateString()}"
                }
                operViewMetadataList.add(
                    OperViewMetadata(
                        OperViewType.TEMP_HOLIDAY,
                        tempHolidayText
                    )
                )
            }
            shopInfo.holidayInfo.takeIf { it.isNotEmpty() }
                ?.let { operViewMetadataList.add(OperViewMetadata(OperViewType.HOLIDAY_INFO, it)) }
            operRecyclerView.adapter = OperListAdapter(operViewMetadataList)
            operRecyclerView.addItemDecoration(HorizontalSpaceItemDecoration(SizeUtils.dp2px(10F)))
        }
    }
}