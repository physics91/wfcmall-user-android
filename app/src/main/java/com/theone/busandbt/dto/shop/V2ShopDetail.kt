package com.theone.busandbt.dto.shop

import android.os.Parcelable
import com.busandbt.code.DeliveryType
import com.busandbt.code.UserStatus
import com.theone.busandbt.dto.menu.MenuGroup
import kotlinx.parcelize.Parcelize

/**
 * 상점 상세 조회
 * @param name 상점명
 * @param status 해당 상점의 상태 [UserStatus]
 * @param minOrderCost 최소 주문 금액
 * @param deliveryTypeList 상점에서 이용가능한 배달유형 리스트 [DeliveryType]
 * @param deliveryDoneMinutes 배송 예상 시간
 * @param packagingDoneMinutes 포장완료 예상 시간
 * @param bundleDoneMinutes 묶음배송완료 예상 시간
 * @param likeCount 찜 개수
 * @param star 평균 별점
 * @param reviewCount 리뷰 개수
 * @param maxDiscountCost 최대 할인 금액(쿠폰)
 * @param writeReview 로그인한 회원이 해당 상점에 리뷰를 썼는지? 값이 true라면 작성한 것
 */
@Parcelize
data class V2ShopDetail(
    val name: String,
    val jibun: String,
    val road: String,
    val addressDetail: String,
    val lat: Double,
    val lng: Double,
    val status: UserStatus,
    val minOrderCost: Int,
    val deliveryTypeList: List<Int>,
    val deliveryDoneMinutes: Int,
    val packagingDoneMinutes: Int,
    val bundleDoneMinutes: Int,
    val imageUrl: String, // TODO - 이미지를 여러개 반환하도록 변경해야됨
    val likeCount: Int,
    val star: Double,
    val reviewCount: Int,
    val maxDiscountCost: Int,
    val packagingDiscountCost: Int,
    val writeReview: Boolean,
    val liked: Boolean,
    val menuGroupList: List<MenuGroup>,
    val paymentTypeNameList: List<String>,
    val minDeliveryCost: Int,
    val maxDeliveryCost: Int,
    val bizNo: String
) : Parcelable