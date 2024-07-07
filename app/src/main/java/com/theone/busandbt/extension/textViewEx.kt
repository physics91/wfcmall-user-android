package com.theone.busandbt.extension

import android.widget.TextView

fun TextView.setTextAppearanceCompat(styleRes: Int) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) setTextAppearance(
        styleRes
    )
    else setTextAppearance(context, styleRes)
}