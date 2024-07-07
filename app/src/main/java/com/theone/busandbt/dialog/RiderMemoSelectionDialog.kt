package com.theone.busandbt.dialog

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.blankj.utilcode.util.ColorUtils
import com.theone.busandbt.R
import com.theone.busandbt.databinding.DialogDeliveryRequestsBinding

/**
 * 배달기사 요청사항 다이어로그
 */
class RiderMemoSelectionDialog(private val defaultRiderMemoText: CharSequence) :
    DataBindingBottomDialog<DialogDeliveryRequestsBinding>(),
    View.OnClickListener {
    override val layoutId: Int = R.layout.dialog_delivery_requests

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            ringTheBellTextView.setOnClickListener(this@RiderMemoSelectionDialog)
            knockTextView.setOnClickListener(this@RiderMemoSelectionDialog)
            callTextView.setOnClickListener(this@RiderMemoSelectionDialog)
            getMySelfTextView.setOnClickListener(this@RiderMemoSelectionDialog)
            mySelfTextView.setOnClickListener(this@RiderMemoSelectionDialog)
            when (defaultRiderMemoText) {
                ringTheBellTextView.text -> {
                    radioGroup.check(R.id.ringTheBellOption)
                    removeColor(
                        ringTheBellTextView,
                        knockTextView,
                        callTextView,
                        getMySelfTextView,
                        mySelfTextView
                    )
                }
                knockTextView.text -> {
                    radioGroup.check(R.id.knockOption)
                    removeColor(
                        knockTextView,
                        ringTheBellTextView,
                        callTextView,
                        getMySelfTextView,
                        mySelfTextView
                    )
                }
                callTextView.text -> {
                    radioGroup.check(R.id.callOption)
                    removeColor(
                        callTextView,
                        ringTheBellTextView,
                        knockTextView,
                        getMySelfTextView,
                        mySelfTextView
                    )
                }
                getMySelfTextView.text -> {
                    radioGroup.check(R.id.getMySelfOption)
                    removeColor(
                        getMySelfTextView,
                        ringTheBellTextView,
                        knockTextView,
                        callTextView,
                        mySelfTextView
                    )
                }
                mySelfTextView.text -> {
                    radioGroup.check(R.id.MySelfOption)
                    removeColor(
                        mySelfTextView,
                        ringTheBellTextView,
                        knockTextView,
                        callTextView,
                        getMySelfTextView
                    )
                }
            }
            radioGroup.setOnCheckedChangeListener { group, checkid ->
                when (checkid) {
                    R.id.ringTheBellOption -> {
                        requestText("문 앞에 두고 벨 눌러주세요")
                        removeColor(
                            ringTheBellTextView,
                            knockTextView,
                            callTextView,
                            getMySelfTextView,
                            mySelfTextView
                        )
                    }
                    R.id.knockOption -> {
                        requestText("벨 누르지 말고 노크 부탁드려요")
                        removeColor(
                            knockTextView,
                            ringTheBellTextView,
                            callTextView,
                            getMySelfTextView,
                            mySelfTextView
                        )
                    }
                    R.id.callOption -> {
                        requestText("도착하기 전 전화 부탁드려요")
                        removeColor(
                            callTextView,
                            ringTheBellTextView,
                            knockTextView,
                            getMySelfTextView,
                            mySelfTextView
                        )
                    }
                    R.id.getMySelfOption -> {
                        requestText("직접 받을게요")
                        removeColor(
                            getMySelfTextView,
                            ringTheBellTextView,
                            knockTextView,
                            callTextView,
                            mySelfTextView
                        )
                    }
                    R.id.MySelfOption -> {
                        requestText("직접 입력")
                        removeColor(
                            mySelfTextView,
                            ringTheBellTextView,
                            knockTextView,
                            callTextView,
                            getMySelfTextView
                        )
                    }
                }
            }
            confirmBtn.setOnClickListener {
                //클릭 버튼 이벤트가 일어난 후 0.2후 팝업창이 닫힘
                Handler().postDelayed({
                    dismiss()
                }, 200)
            }
        }
    }

    private fun requestText(request: String) {
        setFragmentResult("request", bundleOf("requestText" to request))
    }

    //글자색 변경,제거
    private fun removeColor(
        ringTheBellTextView: TextView,
        knockTextView: TextView,
        callTextView: TextView,
        getMySelfTextView: TextView,
        mySelfTextView: TextView,
    ) {
        styleSpanBold(ringTheBellTextView)
        styleSpanNormal(knockTextView)
        styleSpanNormal(callTextView)
        styleSpanNormal(getMySelfTextView)
        styleSpanNormal(mySelfTextView)
        ringTheBellTextView.setTextColor(ColorUtils.getColor(R.color.mainColor))
        knockTextView.setTextColor(Color.parseColor("#111111"))
        callTextView.setTextColor(Color.parseColor("#111111"))
        getMySelfTextView.setTextColor(Color.parseColor("#111111"))
        mySelfTextView.setTextColor(Color.parseColor("#111111"))
    }

    //클릭  폰트 바꾸기
    private fun styleSpanBold(ringTheBellTextView: TextView) {
        val sbb = SpannableStringBuilder(ringTheBellTextView.text.toString())
        sbb.setSpan(
            StyleSpan(Typeface.BOLD), 0, sbb.lastIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ringTheBellTextView.text = sbb
    }

    //클릭  폰트 바꾸기
    private fun styleSpanNormal(ringTheBellTextView: TextView) {
        val sbb = SpannableStringBuilder(ringTheBellTextView.text.toString())
        sbb.setSpan(
            StyleSpan(Typeface.NORMAL), 0, sbb.lastIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ringTheBellTextView.text = sbb
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view) {
                ringTheBellTextView -> {
                    requestText("문 앞에 두고 벨 눌러주세요")
                    removeColor(
                        ringTheBellTextView,
                        knockTextView,
                        callTextView,
                        getMySelfTextView,
                        mySelfTextView
                    )
                    radioGroup.check(R.id.ringTheBellOption)
                }
                knockTextView -> {
                    requestText("벨 누르지 말고 노크 부탁드려요")
                    removeColor(
                        knockTextView,
                        ringTheBellTextView,
                        callTextView,
                        getMySelfTextView,
                        mySelfTextView
                    )
                    radioGroup.check(R.id.knockOption)
                }
                callTextView -> {
                    requestText("도착하기 전 전화 부탁드려요")
                    removeColor(
                        callTextView,
                        knockTextView,
                        ringTheBellTextView,
                        getMySelfTextView,
                        mySelfTextView
                    )
                    radioGroup.check(R.id.callOption)
                }
                getMySelfTextView -> {
                    requestText("직접 받을게요")
                    removeColor(
                        getMySelfTextView,
                        knockTextView,
                        callTextView,
                        ringTheBellTextView,
                        mySelfTextView
                    )
                    radioGroup.check(R.id.getMySelfOption)
                }
                mySelfTextView -> {
                    requestText("직접 입력")
                    removeColor(
                        mySelfTextView,
                        knockTextView,
                        callTextView,
                        getMySelfTextView,
                        ringTheBellTextView
                    )
                    radioGroup.check(R.id.MySelfOption)
                }
            }
        }
    }
}