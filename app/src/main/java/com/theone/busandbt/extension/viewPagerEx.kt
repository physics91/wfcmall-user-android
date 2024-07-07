package com.theone.busandbt.extension

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * viewPager2 내부의 뷰를 터치했을때 터치 이벤트를 intercept 하지않도록 조정한다.
 * 리플렉션을 사용하는 만큼 안드로이드 버전이 바뀔때는 주의해야한다.
 */
fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * 6) // "6" was obtained experimentally
}