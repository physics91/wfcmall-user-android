package com.theone.busandbt.bindingadapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.theone.busandbt.R

object MyInfoBindingAdapter {

    @BindingAdapter("greeting")
    @JvmStatic
    fun greetingForm(view: TextView, memberName: String?) {
        if (memberName != null) view.text = view.context.getString(R.string.greeting, memberName)
    }
}