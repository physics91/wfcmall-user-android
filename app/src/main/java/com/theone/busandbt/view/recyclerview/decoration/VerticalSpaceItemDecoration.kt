package com.theone.busandbt.view.recyclerview.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * @param doFirst true인 경우 첫 항목에도 적용한다.
 * @param doLast true인 경우 마지막 항목에도 적용한다.
 */
class VerticalSpaceItemDecoration(
    private val space: Int,
    private val doFirst: Boolean = true,
    private val doLast: Boolean = true
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        when (val lm = parent.layoutManager) {
            is StaggeredGridLayoutManager -> {
                // TODO
            }
            is GridLayoutManager -> {
                val spanCount = lm.spanCount
                val position = parent.getChildAdapterPosition(view)

                val xAxis = position % spanCount

                val isFirstColumn = xAxis == 0
                val isLastColumn = xAxis == spanCount - 1
                when {
                    isLastColumn && doLast -> outRect.right = space
                    else -> {}
                }
                if (isLastColumn) {
                    if (doLast) outRect.right = space
                } else {
                    outRect.right = space / 2
                }
                if (isFirstColumn) {
                    if (doFirst) outRect.left = space
                } else {
                    outRect.left = space / 2
                }
            }
            is LinearLayoutManager -> {
                if (!doLast) return
                outRect.right = space
            }
            else -> {}
        }
    }
}