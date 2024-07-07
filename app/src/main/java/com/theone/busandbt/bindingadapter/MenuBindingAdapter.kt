package com.theone.busandbt.bindingadapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.theone.busandbt.item.basket.BasketMenuOption

object MenuBindingAdapter {

    /**
     * 리뷰 별점/개수 표시
     */
    @BindingAdapter("menuReviewCount")
    @JvmStatic
    fun menuReviewText(view: TextView, reviewCount: Int) {
        view.text = buildString {
            append("리뷰보기 ")
            append(if (reviewCount > 99) "(99+)" else "($reviewCount)")
        }
    }

    /**
     * 리뷰 별점/개수 표시
     */
    @BindingAdapter("basketMenuOptionDesc")
    @JvmStatic
    fun basketMenuOptionDescForm(view: TextView, optionList: List<BasketMenuOption>) {
        view.text = buildString {
            optionList.forEach { option ->
                append("ㆍ${option.optionGroupName}: ${option.name}")
                if (option.cost > 0) append("(+${option.cost}원)")
                append("\n")
            }
            if (isNotEmpty()) deleteCharAt(length - 1)
        }
    }
}