package com.theone.busandbt.eventbus

import com.theone.busandbt.dialog.MessageDialog

data class ShowMessageDialogEvent(
    val title: String,
    val subTitle: String = "",
    val showCancelButton: Boolean = false,
    val showWarningImageView: Boolean = false,
    val cancelButtonText: String? = null,
    val op: MessageDialog.() -> Unit = {}
)