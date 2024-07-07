package com.theone.busandbt.extension

import com.theone.busandbt.utils.COUPON_USE_DATE_FORMATTER
import java.time.LocalDateTime

/**
 * 지금 날짜 데이터가 리뷰 작성이 가능한 날짜인지만 체크한다.
 * 리뷰 버튼의 노출여부는 여기에 추가로 회원이 리뷰 작성했는지 안했는지도 체크해야한다.
 * 이것만으로 리뷰를 쓸 수 있는 상태인지 체크할 수 없다는 뜻
 */
fun LocalDateTime.isWriteableReviewDateTime(orderDoneDateTime: LocalDateTime): Boolean =
    orderDoneDateTime.plusDays(3).isAfter(this)

fun LocalDateTime.toCouponUseDateString(): String = format(COUPON_USE_DATE_FORMATTER)