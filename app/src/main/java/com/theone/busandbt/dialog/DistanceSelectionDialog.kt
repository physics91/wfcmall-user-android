package com.theone.busandbt.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theone.busandbt.R
import com.theone.busandbt.databinding.DialogDistanceBinding


/**
 * 방문포장 거리 눌렸을 때 나오는 다이어로그
 */
class DistanceSelectionDialog : DataBindingBottomDialog<DialogDistanceBinding>() {
    override val layoutId: Int = R.layout.dialog_distance
    private var requiredInitValue = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            confirmBtn.setOnClickListener {
                setFragmentResult(
                    "distance",
                    bundleOf(
                        "distance" to distance.text.toString().toDouble()
                    )
                )
                dismiss()
            }
            exitBtn.setOnClickListener { dismiss() }
            // OnSeekBarChange 리스너 - Seekbar 값 변경시 이벤트처리 Listener
            seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // onStartTeackingTouch - SeekBar 값 변경위해 첫 눌림에 호출
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // onStopTrackingTouch - SeekBar 값 변경 끝나고 드래그 떼면 호출
                    when (seekBar.progress) {
                        in 0.. 12 -> {
                            seekBar.progress = 0
                            distance.text = "0.5"
                        }
                        in 13..37 -> {
                            seekBar.progress = 25
                            distance.text = "1.0"
                        }
                        in 38..62 -> {
                            seekBar.progress = 50
                            distance.text = "1.5"
                        }
                        in 63..87 -> {
                            seekBar.progress = 75
                            distance.text = "2.0"
                        }
                        else -> {
                            seekBar.progress = 100
                            distance.text = "2.5"
                        }
                    }
                }
            })
            if (requiredInitValue) {
                seekbar.post {
                    seekbar.progress = 0
                }
                requiredInitValue = false
            }
        }
    }

    fun init() {
        requiredInitValue = true
    }
}