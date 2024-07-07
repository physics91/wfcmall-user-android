package com.theone.busandbt.dto.shop

import com.busandbt.code.DeliveryType
import com.busandbt.code.PackagingType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.theone.busandbt.dto.cost.*

/**
 * @param deliveryTypeList 배달유형 리스트 [DeliveryType]
 * @param packagingType 포장유형 [PackagingType]
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ShopInfo(
    val tel: String,
    val addressManageId: Int,
    val jibun: String,
    val road: String,
    val addressDetail: String,
    val lat: Double,
    val lng: Double,
    val intro: String,
    val notice: String,
    val minOrderCost: Int,
    val deliveryType: List<DeliveryType>,
    val deliveryDoneMinutes: Int,
    val packagingType: PackagingType?,
    val packagingCost: Int,
    val packagingDoneMinutes: Int,
    val bundleDoneMinutes: Int,
    val cooInfo: String, // 원산지 정보
    val name: String,
    val realName: String,
    val reptName: String,
    val bizAddress: String,
    val bizNo: String, // 사업자등록번호
    val deliveryCostList: List<DeliveryCost>,
    val dongExtraList: List<DongExtra>,
    val distanceExtraList: List<DistanceExtra>,
    val holidayExtraList: List<HolidayExtra>,
    val areaExtraList: List<AreaExtra>,
    val timeExtraList: List<TimeExtra>,
    val operationTimeList: List<OperationTime>,
    val legalHolidayNameList: List<String>,
    val routineHolidayList: List<RoutineHolidayListItem>,
    val tempHolidayList: List<TemporaryHolidayListItem>,
    val imageUrlList: List<String>,
    val holidayInfo: String
)