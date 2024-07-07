package com.theone.busandbt.utils

import android.content.Intent
import android.net.Uri
import com.blankj.utilcode.util.ActivityUtils

object AppUtils {

    fun openWebsite(url: String) {
        ActivityUtils.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}