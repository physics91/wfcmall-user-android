package com.theone.busandbt.utils

import android.text.InputFilter
import android.text.SpannableString
import androidx.navigation.NavOptions
import com.theone.busandbt.item.Tab
import com.theone.busandbt.jackson.*
import com.busandbt.code.DeliveryType
import com.busandbt.code.OrderStatus
import com.busandbt.code.ServiceType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.theone.busandbt.dto.BusinessInfo
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * 동백통 날짜시간 처리는 모두 이 형식으로 진행한다.
 */
val LOCAL_DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val LOCAL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val LOCAL_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val COUPON_USE_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")

val JACKSON_OBJECT_MAPPER = ObjectMapper().apply {
    registerModule(
        KotlinModule.Builder().build()
            .addSerializer(LocalDateTimeSerializer)
            .addSerializer(LocalDateSerializer)
            .addSerializer(LocalTimeSerializer)
            .addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer)
            .addDeserializer(LocalDate::class.java, LocalDateDeserializer)
            .addDeserializer(LocalTime::class.java, LocalTimeDeserializer)
    )
}

/**
 * RetroFit Json HTTP 헤더
 */
const val HTTP_HEADER_APPLICATION_JSON = "Content-Type: application/json"

val ENABLE_TAB_ARRAY = arrayOf(
    Tab(ServiceType.FOOD_DELIVERY, DeliveryType.INSTANT),
    Tab(ServiceType.FOOD_DELIVERY, DeliveryType.PACKAGING),
    Tab(ServiceType.SHOPPING_MALL, DeliveryType.PARCEL),
)

val DELIVERY_TYPE_TAB_TITLE_ARRAY = arrayOf(
    SpannableString("배달"),
    SpannableString("포장"),
    SpannableString("택배배송")
)

val NAME_INPUT_FILTERS = arrayOf(InputFilter { source, _, _, _, _, _ ->
    val ps = Pattern.compile("^[a-z\\dㄱ-ㅎ가-힣ㆍᆢ]*$")
    if (!ps.matcher(source).matches()) return@InputFilter ""
    return@InputFilter null
})

val ONLY_NUMBER_INPUT_FILTERS = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
    val ps = Pattern.compile("^\\d*$")
    if (!ps.matcher(source).matches()) return@InputFilter ""
    return@InputFilter null
})

/**
 * 사업자 정보
 * 여기에 항목을 추가하면 사업자정보확인 화면에 자동으로 추가된다.
 */
val BUSINESS_INFO_ARRAY = arrayOf(
    BusinessInfo("회사명", "주식회사 부일기획"),
    BusinessInfo("대표", "김진환"),
    BusinessInfo("주소", "부산광역시 해운대구 센텀7로 6 201호, 202호, 203호"),
    BusinessInfo("사업자등록번호", "753-81-00535"),
    BusinessInfo("통신판매업신고", "제 2021-부산해운대-1487호"),
    BusinessInfo("전화", "051-746-9185"),
    BusinessInfo("메일", "plan@ireal.co.kr"),
)

val ORDER_COMPLETE_STATUS_ARRAY = arrayOf(
    OrderStatus.CANCEL,
    OrderStatus.COMPLETE_DELIVERY,
    OrderStatus.COMPLETE_PICK_UP
)
val ORDER_COMPLETE_STATUS_ID_ARRAY = arrayOf(
    OrderStatus.CANCEL.id,
    OrderStatus.COMPLETE_DELIVERY.id,
    OrderStatus.COMPLETE_PICK_UP.id
)
val ORDER_PROCESS_STATUS_ARRAY = arrayOf(
    OrderStatus.NEW_OR_RECEIVING,
    OrderStatus.START_COOKING,
    OrderStatus.START_DELIVERY,
    OrderStatus.COMPLETE_PACKAGING
)
val ORDER_PROCESS_STATUS_ID_ARRAY = arrayOf(
    OrderStatus.NEW_OR_RECEIVING.id,
    OrderStatus.START_COOKING.id,
    OrderStatus.START_DELIVERY.id,
    OrderStatus.COMPLETE_PACKAGING.id
)
