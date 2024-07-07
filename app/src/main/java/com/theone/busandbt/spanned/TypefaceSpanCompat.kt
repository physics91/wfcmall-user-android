package com.theone.busandbt.spanned

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * API 28 밑으로 TypefaceSpan을 사용할 수 없어서 생성함
 */
class TypefaceSpanCompat(private val typeface: Typeface, private val color: Int? = null): MetricAffectingSpan() {
    override fun updateDrawState(tp: TextPaint?) {
        tp?.typeface = typeface
        if (color != null) tp?.color = color
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.typeface = typeface
        if (color != null) textPaint.color = color
    }
}