package com.theone.busandbt.eventbus

data class ChangeSearchKeywordOrSortEvent(
    val tabPosition: Int,
    val keyword: String? = null,
    val minOrderCost: Int? = null,
    val categoryId: Int? = null,
    val distance: Double? = null,
    val coupon: Boolean? = null,
    val packaging: Boolean? = null,
    val shopSortType: Int? = null
)