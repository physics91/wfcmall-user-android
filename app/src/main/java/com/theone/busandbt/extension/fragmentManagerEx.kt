package com.theone.busandbt.extension

import androidx.fragment.app.FragmentManager
import com.theone.busandbt.dialog.MessageDialog

fun FragmentManager.showMessageDialog(
    title: String,
    subTitle: String = "",
    showCancelButton: Boolean = false,
    showWarningImageView: Boolean = false,
    cancelButtonText: String? = null,
    cancelable: Boolean = true,
    op: MessageDialog.() -> Unit = {}
): MessageDialog {
    return MessageDialog(
        title,
        subTitle,
        showCancelButton,
        showWarningImageView,
        cancelButtonText,
        cancelable
    ).apply {
        show(this@showMessageDialog, null)
        op(this)
    }
}