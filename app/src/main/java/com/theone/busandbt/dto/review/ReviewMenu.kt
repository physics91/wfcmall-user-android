package com.theone.busandbt.dto.review

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 리뷰 상품 (주문했던 상품)
 * @param recommended 상품 추천 여부
 */
@Parcelize
data class ReviewMenu(
    val menuId: Int,
    val menuName: String,
    var recommended: Boolean
) : Parcelable