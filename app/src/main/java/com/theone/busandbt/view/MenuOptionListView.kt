package com.theone.busandbt.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat

class MenuOptionListView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    LinearLayoutCompat(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    init {
        orientation = VERTICAL
    }
}