package com.theone.busandbt.item

import android.net.Uri
import com.theone.busandbt.dto.review.ReviewFile

data class SetReviewImage(val imageUri: Uri? = null, val reviewFile: ReviewFile? = null)