package com.theone.busandbt.view.recyclerview.decoration

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val space: Int, private val dividerHeight: Int =0, private val dividerColor: Int?=null) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        if (dividerColor != null) {
            color = dividerColor
        }
        style = Paint.Style.FILL
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = space
        outRect.bottom = dividerHeight
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (dividerColor != null) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val layoutParams = child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + layoutParams.bottomMargin
                val bottom = top + dividerHeight
                c.drawRect(0f, top.toFloat(), parent.width.toFloat(), bottom.toFloat(), paint)
            }
        }
    }
}