package com.theone.busandbt.dto.review

data class ReviewStatistics(
    val rankedMenuList: List<String>,
    val star: Double,
    val reviewCount: Int,
    val shopReplyCount: Int
)