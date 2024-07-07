package com.theone.busandbt.dto.shop

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MallShopDetail(
    val name: String,
    val status: Int,
    val imageUrlList: List<String>,
    val likeCount: Int,
    val star: Double,
    val reviewCount: Int,
    val maxDiscountCost: Int,
    val writeReview: Boolean,
    val liked: Boolean,
    val paymentTypeNameList: List<String>,
    val deliveryCost: Int,
    val orderCostForFree: Int
) : Parcelable