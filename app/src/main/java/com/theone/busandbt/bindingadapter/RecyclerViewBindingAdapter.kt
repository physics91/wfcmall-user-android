package com.theone.busandbt.bindingadapter

import android.graphics.drawable.Drawable
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.view.recyclerview.decoration.EasyDividerDecoration

object RecyclerViewBindingAdapter {

    /**
     * 할인 매장 쿠폰 할인 텍스트 형식을 지정한다.
     */
    @BindingAdapter("newDivider")
    @JvmStatic
    fun divider(
        view: RecyclerView,
        newDivider: Drawable
    ) {
        view.addItemDecoration(EasyDividerDecoration(newDivider))
    }
}