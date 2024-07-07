package com.theone.busandbt.fragment.join

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ColorUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentJoinServiceAgreeBinding
import com.theone.busandbt.item.JoinAgreeItem
import com.theone.busandbt.type.Nice24ActionType
import com.theone.busandbt.utils.AppUtils
import com.busandbt.code.JoinType
import com.theone.busandbt.extension.navigate

/**
 * 회원가입 - 약관동의 화면
 */
class JoinServiceAgreeFragment : BaseJoinFragment<FragmentJoinServiceAgreeBinding>(),
    EnabledGoBackButton, View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_join_service_agree
    override val actionBarTitle: String = EMPTY_ACTION_BAR_TITLE
    private val joinAgreeItem = ArrayList<JoinAgreeItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            joinAgreeItem.add(JoinAgreeItem(ageCheckbox, ageTextView))
            joinAgreeItem.add(JoinAgreeItem(serviceCheckbox, serviceTextView))
            joinAgreeItem.add(JoinAgreeItem(privacyCheckbox, privacyTitleTextView))
            nextButton.text = when (joinInfoViewModel.joinType) {
                JoinType.NORMAL -> "다음"
                else -> "본인인증하고 회원가입"
            }
            initOnClickEvent()
        }
    }

    /**
     * 클릭 이벤트 모음
     */
    private fun initOnClickEvent() {
        with(binding) {
            allTextTextView.setOnClickListener(this@JoinServiceAgreeFragment)
            allCheckBox.setOnClickListener(this@JoinServiceAgreeFragment)
            ageTextView.setOnClickListener(this@JoinServiceAgreeFragment)
            ageCheckbox.setOnClickListener(this@JoinServiceAgreeFragment)
            serviceTextView.setOnClickListener(this@JoinServiceAgreeFragment)
            serviceCheckbox.setOnClickListener(this@JoinServiceAgreeFragment)
            privacyCheckbox.setOnClickListener(this@JoinServiceAgreeFragment)
            privacyTitleTextView.setOnClickListener(this@JoinServiceAgreeFragment)
            nextButton.setOnClickListener(this@JoinServiceAgreeFragment)
            privacyDetailButton.setOnClickListener(this@JoinServiceAgreeFragment)
            serviceDetailButton.setOnClickListener(this@JoinServiceAgreeFragment)
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            //약관 동의 클릭시 버튼 활성화 조건 여부 확인
            buttonConverter()
            when (view) {
                privacyDetailButton -> view.navigate(R.id.privacy_main_graph)
                serviceDetailButton -> AppUtils.openWebsite("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/builplanService.html")
                //전체 동의 클릭 시 모든 체크박스 on, off
                allTextTextView, allCheckBox -> {
                    if (view !is CheckBox) allCheckBox.isChecked = !allCheckBox.isChecked
                    for (index in 0..2) {
                        joinAgreeItem[index].checkBoxSwitchItem.isChecked =
                            allCheckBox.isChecked
                        checkBoxColorSwitch(
                            allCheckBox.isChecked,
                            joinAgreeItem[index].checkBoxTextItem
                        )
                    }
                    buttonConverter()
                }
                ageTextView, ageCheckbox -> {
                    if (view !is CheckBox) ageCheckbox.isChecked = !ageCheckbox.isChecked
                    checkBoxColorSwitch(
                        ageCheckbox.isChecked,
                        joinAgreeItem[0].checkBoxTextItem
                    )
                    if (!joinAgreeItem[0].checkBoxSwitchItem.isChecked) {
                        allCheckBox.isChecked = false
                    }
                }
                //sixth체크박스는 하단의 작은 텍스트뷰 세가지를 모두 제어, 그리고 first~fifth체크박스의 기능 추가
                serviceTextView, serviceCheckbox -> {
                    if (view !is CheckBox) serviceCheckbox.isChecked = !serviceCheckbox.isChecked
                    checkBoxColorSwitch(
                        serviceCheckbox.isChecked,
                        joinAgreeItem[1].checkBoxTextItem
                    )
                    if (!joinAgreeItem[1].checkBoxSwitchItem.isChecked) {
                        allCheckBox.isChecked = false
                    }
                }
                privacyTitleTextView, privacyCheckbox -> {
                    if (view !is CheckBox) privacyCheckbox.isChecked = !privacyCheckbox.isChecked
                    checkBoxColorSwitch(
                        privacyCheckbox.isChecked,
                        joinAgreeItem[2].checkBoxTextItem
                    )
                    if (!joinAgreeItem[2].checkBoxSwitchItem.isChecked) {
                        allCheckBox.isChecked = false
                    }
                }
                nextButton -> {
                    when (joinInfoViewModel.joinType) {
                        JoinType.NORMAL -> findNavController().navigate(R.id.action_joinServiceAgreeFragment_to_joinPhoneNoCheckFragment)
                        else -> {
                            findNavController().navigate(
                                JoinServiceAgreeFragmentDirections.actionJoinServiceAgreeFragmentToNice24Fragment(
                                    Nice24ActionType.SNS_JOIN.id
                                )
                            )
                        }
                    }
                }
            }
            allCheckBoxOn()
            buttonConverter()
        }
    }

    //체크박스가 모두 on일때 전체 선택의 체크도 on으로 바꿔주는 함수
    private fun allCheckBoxOn() {
        var checkBoxCounter = 0
        for (index in 0 until joinAgreeItem.size) {
            if (joinAgreeItem[index].checkBoxSwitchItem.isChecked) ++checkBoxCounter
        }
        if (checkBoxCounter == joinAgreeItem.size) binding.allCheckBox.isChecked = true
    }

    //체크박스 클릭시 글자색 변경
    private fun checkBoxColorSwitch(checked: Boolean, place: TextView) {
        if (checked) place.setTextColor(ColorUtils.getColor(R.color.mainColor))
        else place.setTextColor(Color.parseColor("#AEAEAE"))
    }

    //필수 동의 체크시 다음 버튼을 활성화하는 함수
    private fun buttonConverter() {
        with(binding.nextButton) {
            isEnabled = if (joinAgreeItem[0].checkBoxSwitchItem.isChecked &&
                joinAgreeItem[1].checkBoxSwitchItem.isChecked && joinAgreeItem[2].checkBoxSwitchItem.isChecked
            ) {
                setBackgroundResource(R.drawable.bg_round_27dp_main_color)
                true
            } else {
                setBackgroundResource(R.drawable.bg_round_27dp_disabled_color)
                false
            }
        }
    }
}