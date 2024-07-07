package com.theone.busandbt.dto.review.request

data class SetReviewRequest(
    val content: String? = null,
    val star: Double? = null,
    val isVisible: Boolean? = null,
    val newImageList: List<AddReviewRequest.ReviewFile> = emptyList(),
    val removedImageIdList: List<Int> = emptyList(),
    val changedMenuList: List<AddReviewRequest.ReviewMenu> = emptyList()
)