package com.theone.busandbt.dto.promotion

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 이벤트 배너 데이터 (메인 화면의 배너 슬라이드 같은 곳에 들어감)
 */
@Parcelize
data class PromotionListItem(
    val id: Int,
    val title: String,
    val imageUrl: String
) : Parcelable