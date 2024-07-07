package com.theone.busandbt.dto.address
/**
 * 주소 등록 성공 응답 데이터
 * @param deliveryAddressId : 주소 등록 되어있는 고유 아이디
 */
data class AddDeliveryAddressIdResponse(
    val deliveryAddressId : Int,
)