package com.theone.busandbt.dto.review

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MemberReviewListItem(
    val id: Int,
    val shopId: Int,
    val shopName: String,
    val content: String,
    val star: Double,
    val isVisible: Boolean,
    val createDateTime: String,
    val orderDoneDateTime: String,
    val imageFileList: List<ReviewFile>,
    val menuList: List<ReviewMenu>,
) : Parcelable