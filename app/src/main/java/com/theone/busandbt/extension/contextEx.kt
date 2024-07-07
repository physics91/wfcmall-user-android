package com.theone.busandbt.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.FontRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.theone.busandbt.R

fun Context.showCompleteToast(@LayoutRes layoutResId: Int) {
    val myToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
    val toastLayout: View =
        LayoutInflater.from(this).inflate(layoutResId, null)
    myToast.view = toastLayout
    myToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
    myToast.show()
}

fun Context.hasLocationPermission() = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_COARSE_LOCATION
) == PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_FINE_LOCATION
) == PackageManager.PERMISSION_GRANTED

fun Context.getTypeface(@FontRes font: Int): Typeface? = ResourcesCompat.getFont(this, font)
val Context.sultBold: Typeface get() = getTypeface(R.font.sult_bold)!!
val Context.sultSemiBold: Typeface get() = getTypeface(R.font.sult_semibold)!!
val Context.sultMedium: Typeface get() = getTypeface(R.font.sult_medium)!!
val Context.sultRegular: Typeface get() = getTypeface(R.font.sult_regular)!!