package com.theone.busandbt.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.widget.NestedScrollView

class StickyHeaderListScrollView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attributeSet, defStyleAttr) {

    private val stickyHeaderViewList = ArrayList<View>()
    private var restoreScrollY: Int? = null

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        stickyHeaderViewList.clear()
        val child = children.firstOrNull()
        if (child != null) findStickyViews(child)
        val rsy = restoreScrollY
        if (rsy != null) {
            formStickyViews(rsy)
            restoreScrollY = null
        }
    }

    override fun onScrollChanged(mScrollX: Int, mScrollY: Int, oldX: Int, oldY: Int) {
        super.onScrollChanged(mScrollX, mScrollY, oldX, oldY)
        if (stickyHeaderViewList.isEmpty()) restoreScrollY = mScrollY
        formStickyViews(mScrollY)
    }

    private fun formStickyViews(scrollY: Int) {
        var weight = 0
        stickyHeaderViewList.forEach {
            if (it.top - weight <= scrollY) {
                it.translationY = (scrollY + weight - it.top).toFloat()
                setTranslationZ(it, 1f)
            } else {
                it.translationY = 0f
                setTranslationZ(it, 0f)
            }
            weight += it.measuredHeight
        }
    }

    private fun findStickyViews(v: View) {
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                val tag = getStringTagForView(v.getChildAt(i))
                if (tag != null && tag.contains(STICKY_TAG)) {
                    stickyHeaderViewList.add(v.getChildAt(i))
                } else if (v.getChildAt(i) is ViewGroup) {
                    findStickyViews(v.getChildAt(i))
                }
            }
        } else {
            val tag = v.tag as? String
            if (tag != null && tag.contains(STICKY_TAG)) {
                stickyHeaderViewList.add(v)
            }
        }
    }

    private fun getStringTagForView(v: View): String? {
        val tagObject = v.tag
        return tagObject as? String
    }

    private fun setTranslationZ(view: View, translationZ: Float) {
        ViewCompat.setTranslationZ(view, translationZ)
    }

    companion object {
        private const val STICKY_TAG = "sticky"
    }
}