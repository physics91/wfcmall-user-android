package com.theone.busandbt.extension

import android.util.Log

fun infoLog(tag: String?, value: Any?) = Log.i(tag, value.toString())
fun debugLog(tag: String?, value: Any?) = Log.d(tag, value.toString())
fun errorLog(tag: String?, value: Any?) = Log.e(tag, value.toString())