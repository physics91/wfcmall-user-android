package com.theone.busandbt.fragment.card

import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.listener.BackButtonInterceptor
import com.theone.busandbt.view.CustomKeyboard

abstract class SecurityKeyboardFragment<VDB : ViewDataBinding> : DataBindingFragment<VDB>(),
    BackButtonInterceptor {
    abstract val securityKeyboard: CustomKeyboard

    override fun interceptOnBackButtonClicked(): Boolean {
        if (!securityKeyboard.isVisible) return true
        hide()
        return false
    }

    abstract fun hide()
    abstract fun show()
}