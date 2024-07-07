package com.theone.busandbt.dto.review

/**
 * 리뷰 리스트 데이터
 * @param id 리뷰 고유번호, reviewId
 * @param writerName 작성자명
 * @param content 리뷰 내용
 * @param star 별점, 5점 만점에 0.5점 단위
 * @param createDateTime 리뷰 작성일자
 */
data class ReviewListItem(
    val id: Int,
    val writerName: String,
    val writerProfileImageUrl: String,
    val content: String,
    val star: Double,
    val isVisible: Boolean,
    val createDateTime: String,
    val imageFileList: List<ReviewFile>,
    val menuList: List<ReviewMenu>,
    val shopReply: ShopReply? = null
)