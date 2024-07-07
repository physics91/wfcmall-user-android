package com.theone.busandbt.dto.category

import android.os.Parcelable
import com.busandbt.code.CategoryType
import kotlinx.parcelize.Parcelize

/**
 * 카테고리 데이터
 * @param upperCategoryId 상위 카테고리 고유번호
 * @param type 카테고리 분류 [CategoryType]
 * @param visibleIndex 카테고리 노출 순서
 * @param imageUrl 카테고리 이미지 URL
 * @param enabled 카테고리 활성화 여부
 */
@Parcelize
data class CategoryListItem(
    val id: Int,
    val upperCategoryId: Int,
    val serviceType: Int,
    val name: String,
    val type: Int,
    val imageUrl: String,
    val lowerCategoryList: List<CategoryListItem>
): Parcelable