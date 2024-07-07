package com.theone.busandbt.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.SizeUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.DialogMessageBinding

class MessageDialog(
    private val title: String,
    private val subTitle: String = "",
    private val showCancelButton: Boolean = false,
    private val showWarningImageView: Boolean = false,
    private val cancelButtonText: String? = null,
    private val cancelable: Boolean = true
) : DataBindingDialog<DialogMessageBinding>() {
    override val layoutId: Int = R.layout.dialog_message

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            isCancelable = cancelable
            titleTextView.text = title
            subTitleTextView.text = subTitle
            subTitleTextView.isVisible = subTitle.isNotEmpty()
            cancelButton.isVisible = showCancelButton
            cancelButton.setOnClickListener { dismiss() }
            if (cancelButtonText != null) cancelButton.text = cancelButtonText
            doneButton.setBackgroundResource(if (showCancelButton) R.drawable.bg_round_24dp_main_color else R.drawable.bg_round_24dp_main_color)
            doneButton.setOnClickListener { dismiss() }
            warningImageView.isVisible = showWarningImageView
            if (!showWarningImageView) {
                val dp = if (subTitle.isNotEmpty()) 50f else 78f
                (titleTextView.layoutParams as MarginLayoutParams).topMargin = SizeUtils.dp2px(dp)
            }
        }
    }

    fun onDoneButtonClick(buttonText: String = "확인", op: View.() -> Unit) {
        lifecycleScope.launchWhenStarted {
            with(binding.doneButton) {
                text = buttonText
                setOnClickListener {
                    op(it)
                }
            }
        }
    }

    fun onCancelButtonClick(buttonText: String = cancelButtonText ?: "취소", op: View.() -> Unit) {
        lifecycleScope.launchWhenStarted {
            with(binding.cancelButton) {
                text = buttonText
                setOnClickListener {
                    op(it)
                }
            }
        }
    }
}