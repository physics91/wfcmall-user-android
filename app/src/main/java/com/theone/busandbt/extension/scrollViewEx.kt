package com.theone.busandbt.extension

import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.widget.NestedScrollView
import kotlin.math.abs

internal fun NestedScrollView.computeDistanceToView(view: View): Int {
    return abs(
        calculateRectOnScreen(this).top - (this.scrollY + calculateRectOnScreen(
            view
        ).top)
    )
}

internal fun calculateRectOnScreen(view: View): Rect {
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    return Rect(
        location[0],
        location[1],
        location[0] + view.measuredWidth,
        location[1] + view.measuredHeight
    )
}

fun NestedScrollView.scrollToView(view: View, offset: Int = 0) {
    val y = computeDistanceToView(view)
    this.scrollTo(0, y + offset)
}

fun NestedScrollView.smoothScrollToView(
    view: View,
    marginTop: Int = 0,
    maxDuration: Long = 500L,
    onEnd: () -> Unit = {}
) {
    if (this.getChildAt(0).height <= this.height) { // 스크롤의 의미가 없다.
        onEnd()
        return
    }
    val y = computeDistanceToView(view) - marginTop
    val ratio = abs(y - this.scrollY) / (this.getChildAt(0).height - this.height).toFloat()
    ObjectAnimator.ofInt(this, "scrollY", y).apply {
        duration = (maxDuration * ratio).toLong()
        interpolator = AccelerateDecelerateInterpolator()
        doOnEnd {
            onEnd()
        }
        start()
    }
}