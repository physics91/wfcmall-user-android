package com.theone.busandbt.extension

import android.view.Window

fun Window.getSoftInputMode() : Int {
    return attributes.softInputMode
}