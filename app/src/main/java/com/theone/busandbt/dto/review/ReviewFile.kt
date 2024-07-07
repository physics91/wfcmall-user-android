package com.theone.busandbt.dto.review

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewFile(
    val id: Int,
    val path: String
) : Parcelable