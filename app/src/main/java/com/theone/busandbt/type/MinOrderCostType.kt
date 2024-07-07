package com.theone.busandbt.type

/**
 * 최소주문금액 필터 데이터
 */
enum class MinOrderCostType(val minOrderCost: Int, val title: String) {
    ALL(0, "전체"),
    UNDER_5000(5000, "5,000원 이하"),
    UNDER_10000(10000, "10,000원 이하"),
    UNDER_12000(12000, "12,000원 이하"),
    UNDER_15000(15000, "15,000원 이하"),
    UNDER_20000(20000, "20,000원 이하"),
    ;
}