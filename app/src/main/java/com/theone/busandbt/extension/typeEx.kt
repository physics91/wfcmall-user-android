package com.theone.busandbt.extension

import com.busandbt.code.*
import java.time.DayOfWeek

fun Period.desc(): String = when(this) {
    Period.EVERY_FIRST_WEEK -> "매달 첫째주"
    Period.EVERY_SECOND_WEEK -> "매달 둘째주"
    Period.EVERY_THIRD_WEEK -> "매달 셋째주"
    Period.EVERY_FOURTH_WEEK -> "매달 넷째주"
    Period.EVERY_FIFTH_WEEK -> "매달 다섯째주"
    Period.EVERY_MONTH -> "매달"
    Period.EVERY_WEEK -> "매주"
    Period.EVERY_LAST_WEEK -> "매달 마지막주"
}

fun DayOfWeek.desc(): String = when (this) {
    DayOfWeek.MONDAY -> "월요일"
    DayOfWeek.TUESDAY -> "화요일"
    DayOfWeek.WEDNESDAY -> "수요일"
    DayOfWeek.THURSDAY -> "목요일"
    DayOfWeek.FRIDAY -> "금요일"
    DayOfWeek.SATURDAY -> "토요일"
    DayOfWeek.SUNDAY -> "일요일"
}

fun ServiceType.desc(): String = when (this) {
    ServiceType.FOOD_DELIVERY -> "음식점"
    ServiceType.TRADITION -> "전통시장"
    ServiceType.SHOPPING_MALL -> "쇼핑몰"
    else -> ""
}

fun DeliveryType.desc(serviceType: ServiceType? = null): String = when (this) {
    DeliveryType.INSTANT -> when (serviceType) {
        ServiceType.FOOD_DELIVERY -> "배달"
        ServiceType.TRADITION -> "바로배달"
        else -> "배달"
    }
    DeliveryType.PACKAGING -> "포장"
    DeliveryType.BUNDLE -> "묶음배송"
    DeliveryType.PARCEL -> "택배배송"
    DeliveryType.OUR_TOWN -> "우리동네"
    else -> ""
}

fun OrderStatus.desc(): String = when (this) {
    OrderStatus.NEW_OR_RECEIVING -> "접수중"
    OrderStatus.START_COOKING -> "조리중"
    OrderStatus.START_DELIVERY -> "배달시작"
    OrderStatus.COMPLETE_PACKAGING -> "포장완료"
    OrderStatus.COMPLETE_DELIVERY -> "배달완료"
    OrderStatus.CANCEL -> "주문취소"
    else -> "기타"
}

fun PaymentType.desc(): String = when (this) {
    PaymentType.PREPAY_ZEROPAY -> "제로페이(선결제)"
    PaymentType.MEET_CASH -> "만나서 현금결제"
    PaymentType.MEET_CARD -> "만나서 카드결제"
    PaymentType.ZEROPAY -> "제로페이"
    PaymentType.PREPAY_CARD -> "카드결제(선결제)"
    PaymentType.PREPAY_LOCAL_CURRENCY -> "동백전(선결제)"
    PaymentType.PREPAY_ONNURI -> "온누리상품권(선결제)"
    PaymentType.LOCAL_CURRENCY -> "동백전"
    PaymentType.ONNURI -> "온누리상품권"
}

fun CancelType.desc(): String = when (this) {
    CancelType.DISABLED_AREA -> "배달불가지역" // 배달불가지역
    CancelType.MENU_OR_SHOP_NOT_MATCH -> "메뉴 및 업소정보 다름" // 메뉴 및 업소정보 다름
    CancelType.STUFF_ZERO -> "재료소진" // 재료소진
    CancelType.DELIVERY_DELAY -> "배달지연" // 배달지연
    CancelType.CUSTOMER_INFO_WRONG -> "고객정보 부정확" // 고객정보 부정확
    CancelType.CUSTOMER_REQUEST -> "고객요청" // 고객요청
    CancelType.CHANGED_MIND -> "단순변심" // 단순변심
    CancelType.WRONG_ORDER -> "잘못된주문" // 잘못된주문
    CancelType.SOLD_OUT -> "품절" // 품절
    CancelType.DUPLICATE_RESERVATION -> "중복예약" // 중복예약
    CancelType.RESERVATION_INFO_CHANGED -> "예약정보변경" // 예약정보변경
    CancelType.ETC -> "기타" // 기타
}

fun ShopSortType.desc(): String = when (this) {
    ShopSortType.NEAR -> "가까운 순"
    ShopSortType.HIGHER_STAR -> "별점 높은 순"
    ShopSortType.LOWER_DELIVERY_COST -> "배달비 낮은 순"
    ShopSortType.MANY_LIKE -> "찜 많은 순"
    ShopSortType.MANY_ORDER -> "주문 많은 순"
    ShopSortType.FAST_DELIVERY -> "배달 빠른 순"
    else -> "사용하면 안되는 유형"
}

fun MenuSortType.desc(): String = when (this) {
    MenuSortType.RECENT_ADD -> "최신 등록 순"
    MenuSortType.MANY_REVIEW -> "리뷰 많은 순"
    MenuSortType.HIGHER_MENU_COST -> "가격 높은 순"
    MenuSortType.LOWER_MENU_COST -> "가격 낮은 순"
    MenuSortType.MANY_ORDER -> "주문 많은 순"
    else -> "사용하면 안되는 유형"
}

fun LikeSortType.desc(): String = when(this) {
    LikeSortType.RECENT_ADDED -> "최근 추가한 순"
    LikeSortType.OFTEN_ORDER -> "자주 주문한 순"
    LikeSortType.RECENT_ORDER -> "최근 주문한 순"
}

fun ReviewSortType.desc(): String = when(this) {
    ReviewSortType.REGISTER_DATE -> "최근 추가한 순"
    ReviewSortType.HIGHER_STAR -> "별점 높은 순"
    ReviewSortType.LOWER_STAR -> "별점 낮은 순"
}

fun ParcelStatus.desc(): String = when(this) {
    ParcelStatus.SUSPEND_RECEIPT -> "결제완료"
    ParcelStatus.PROCESS -> "배송중"
    ParcelStatus.COMPLETE -> "배송완료"
    ParcelStatus.COMPLETE_RECEIPT -> "접수완료"
    ParcelStatus.CANCEL -> "배송취소"
}

fun CourierType.desc(): String = when(this) {
    CourierType.CJ -> "CJ대한통운"
    CourierType.POST_OFFICE -> "우체국택배"
    CourierType.HAN_JIN -> "한진택배"
    CourierType.LOTTE -> "롯데택배"
    CourierType.RO_GEN -> "로젠택배"
    CourierType.GS25 -> "편의점택배(GS25)"
    CourierType.CU -> "CU 편의점택배"
    CourierType.KYEONG_DONG -> "경동택배"
    CourierType.DAE_SIN -> "대신택배"
    CourierType.EMS -> "EMS"
    CourierType.DHL -> "DHL"
    else -> ""
}

fun CardType.desc(): String = when(this) {
    CardType.BC -> "비씨카드"
    CardType.SHINHAN -> "신한카드"
    CardType.NONGHYUB -> "농협카드"
    CardType.KB -> "국민카드"
    CardType.LOTTE -> "롯데카드"
    CardType.SAMSUNG -> "삼성카드"
    CardType.CITY -> "시티카드"
    CardType.HYUNDAI -> "현대카드"
    CardType.HANA -> "하나카드"
    CardType.WOORI -> "우리카드"
    CardType.SU_HYUB -> "수협카드"
    CardType.K_BANK -> "K뱅크카드"
    CardType.KWANG_JU -> "광주카드"
    CardType.JEJU -> "제주카드"
    else -> ""
}