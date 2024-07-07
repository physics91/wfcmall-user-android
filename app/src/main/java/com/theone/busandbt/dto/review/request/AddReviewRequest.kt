package com.theone.busandbt.dto.review.request

import com.busandbt.code.ChannelType
import com.busandbt.code.ReviewType
import com.busandbt.code.ReviewWriterType

/**
 * 리뷰 등록 요청 데이터
 * @param writerId 리뷰 작성자의 고유번호, 여기선 memberId만 들어간다.
 * @param writerType 리뷰 작성자 유형, 여기선 회원만 들어간다. [ReviewWriterType]
 * @param type 리뷰 유형 [ReviewType]
 */
data class AddReviewRequest(
    val channelType: Int = ChannelType.DONG_BAEK.id,
    val shopId: Int,
    val orderId: String,
    val writerId: Int,
    val writerType: Int = ReviewWriterType.MEMBER.id,
    val content: String,
    val type: Int = ReviewType.MAIN.id,
    val star: Double,
    val isVisible: Boolean,
    val upperReviewId: Int? = null,
    val fileList: List<ReviewFile> = emptyList(),
    val menuList: List<ReviewMenu> = emptyList()
) {

    /**
     * 리뷰 파일 (요청용)
     * @param encodedFile 파일의 바이트코드를 base64로 encode해서 입력한다.
     */
    data class ReviewFile(
        val encodedFile: String,
        val fileName: String,
        val fileExtension: String
    )

    /**
     * 리뷰 상품 (요청용)
     * @param recommended 상품 추천 여부
     */
    data class ReviewMenu(
        val menuId: Int,
        val recommended: Boolean
    )
}