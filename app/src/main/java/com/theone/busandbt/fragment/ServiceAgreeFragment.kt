package com.theone.busandbt.fragment

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SPUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentServiceAgreeBinding
import com.theone.busandbt.fragment.join.BaseJoinFragment
import com.theone.busandbt.item.JoinAgreeItem
import com.theone.busandbt.utils.APP_SETTINGS_KEY
import com.theone.busandbt.utils.AppUtils

/**
 * 회원가입 - 약관동의 화면
 */
class ServiceAgreeFragment : BaseJoinFragment<FragmentServiceAgreeBinding>(), EnabledGoBackButton,
    View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_service_agree
    private val joinAgreeItem = ArrayList<JoinAgreeItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //체크여부에 따라 색을 바꿀 수 있도록 (체크박스, 텍스트) 형식의 배열 요소 추가
        with(binding) {
            with(joinAgreeItem) {
                add(JoinAgreeItem(locationCheckbox, locationTextView))
                add(JoinAgreeItem(eventCheckbox, eventTextView))
            }
        }
        initClickListener()
    }

    /**
     * TODO - 추후에 자체 ViewModel로 정의할 것
     */
    override fun onResume() {
        super.onResume()
        with(binding) {
            joinAgreeItem.add(JoinAgreeItem(locationCheckbox, locationTextView))
            joinAgreeItem.add(JoinAgreeItem(eventCheckbox, eventTextView))
            locationTextView.changeTextColorByCheckBoxState(locationCheckbox.isChecked)
            eventTextView.changeTextColorByCheckBoxState(eventCheckbox.isChecked)
            val sp = SPUtils.getInstance(APP_SETTINGS_KEY)
            appNotificationTextView.isSelected = sp.getBoolean("receiveAppAlert", false)
            appNotificationTextView.changeTextColorBySelectState()
            messageNotificationTextView.isSelected = sp.getBoolean("receiveSmsAlert", false)
            messageNotificationTextView.changeTextColorBySelectState()
            eventCheckBoxOn()
            eventTextView.changeTextColorByCheckBoxState(eventCheckbox.isChecked)
            settingsForNextButton()
        }
    }

    /**
     * 클릭 리스너 재설정
     * 재설정된 리스너는 onClick에서 활용됨
     */
    private fun initClickListener() {
        with(binding) {
            allCheckBox.setOnClickListener(this@ServiceAgreeFragment)
            allCheckTextView.setOnClickListener(this@ServiceAgreeFragment)
            locationCheckbox.setOnClickListener(this@ServiceAgreeFragment)
            locationTextView.setOnClickListener(this@ServiceAgreeFragment)
            eventCheckbox.setOnClickListener(this@ServiceAgreeFragment)
            eventTextView.setOnClickListener(this@ServiceAgreeFragment)
            appNotificationTextView.setOnClickListener(this@ServiceAgreeFragment)
            messageNotificationTextView.setOnClickListener(this@ServiceAgreeFragment)
            nextButton.setOnClickListener(this@ServiceAgreeFragment)
            locationDetailTextView.setOnClickListener(this@ServiceAgreeFragment)
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            //약관 동의 클릭시 버튼 활성화 조건 여부 확인
            when (view) {
                locationDetailTextView -> { // 위치기반 서비스 이용약관
                    AppUtils.openWebsite("https://ireal-file.s3.ap-northeast-2.amazonaws.com/docs/builplanLocationService.html")
                }
                //전체 동의 클릭 시 모든 체크박스 on, off
                allCheckBox, allCheckTextView -> {
                    if (view == allCheckTextView) {
                        allCheckBox.isChecked = !allCheckBox.isChecked
                    }

                    for (index: Int in 0..1) {
                        joinAgreeItem[index].checkBoxSwitchItem.isChecked =
                            allCheckBox.isChecked
                        checkBoxColorSwitch(
                            allCheckBox.isChecked,
                            joinAgreeItem[index].checkBoxTextItem
                        )
                    }

                    if (allCheckBox.isChecked) {
                        appNotificationTextView.setTextColor(ColorUtils.getColor(R.color.mainColor))
                        messageNotificationTextView.setTextColor(ColorUtils.getColor(R.color.mainColor))
                    } else {
                        appNotificationTextView.setTextColor(ColorUtils.getColor(R.color.agreeDisableColor))
                        messageNotificationTextView.setTextColor(ColorUtils.getColor(R.color.agreeDisableColor))
                    }
                }

                locationCheckbox, locationTextView -> {
                    if (view == locationTextView) {
                        locationCheckbox.isChecked = !locationCheckbox.isChecked
                    }

                    checkBoxColorSwitch(
                        locationCheckbox.isChecked,
                        joinAgreeItem[0].checkBoxTextItem
                    )
                    if (!joinAgreeItem[0].checkBoxSwitchItem.isChecked) {
                        allCheckBox.isChecked = false
                    }
                }

                eventCheckbox, eventTextView -> {
                    if (view == eventTextView) {
                        eventCheckbox.isChecked = !eventCheckbox.isChecked
                    }

                    checkBoxColorSwitch(
                        eventCheckbox.isChecked,
                        joinAgreeItem[1].checkBoxTextItem
                    )
                    if (!joinAgreeItem[1].checkBoxSwitchItem.isChecked) {
                        allCheckBox.isChecked = false
                        appNotificationTextView.setTextColor(ColorUtils.getColor(R.color.agreeDisableColor))
                        messageNotificationTextView.setTextColor(ColorUtils.getColor(R.color.agreeDisableColor))
                    } else {
                        appNotificationTextView.setTextColor(ColorUtils.getColor(R.color.mainColor))
                        messageNotificationTextView.setTextColor(ColorUtils.getColor(R.color.mainColor))
                    }
                }

                //세가지 작은 텍스트뷰는 on일시 sixth체크박스와 전체동의 체크박스 on, 하나라도 off시 sixth체크박스와 전체동의 off
                appNotificationTextView -> {
                    textViewColorSwitch(appNotificationTextView)
                    if (appNotificationTextView.currentTextColor == ColorUtils.getColor(R.color.agreeDisableColor)) {
                        eventCheckbox.isChecked = false
                        allCheckBox.isChecked = false
                        checkBoxColorSwitch(
                            eventCheckbox.isChecked,
                            joinAgreeItem[1].checkBoxTextItem
                        )
                    }
                    eventCheckboxOn()
                    checkBoxColorSwitch(
                        eventCheckbox.isChecked,
                        joinAgreeItem[1].checkBoxTextItem
                    )
                }
                messageNotificationTextView -> {
                    textViewColorSwitch(messageNotificationTextView)
                    if (messageNotificationTextView.currentTextColor == ColorUtils.getColor(R.color.agreeDisableColor)) {
                        eventCheckbox.isChecked = false
                        allCheckBox.isChecked = false
                        checkBoxColorSwitch(
                            eventCheckbox.isChecked,
                            joinAgreeItem[1].checkBoxTextItem
                        )
                    }
                    eventCheckboxOn()
                    checkBoxColorSwitch(
                        eventCheckbox.isChecked,
                        joinAgreeItem[1].checkBoxTextItem
                    )
                }
                nextButton -> {
                    //현재 권한여부를 묻는 기능이 회원가입에서 구현되지 않음. 임시로 권한 여부를 확인하도록 한다.
                    val permissions: Array<String> = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    requestPermissions(
                        permissions,
                        1
                    )
                }
            }
        }
        allCheckBoxOn()
        settingsForNextButton()
    }

    // TODO - 거절했을때 처리가 필요함
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        if (requestCode == 1) {
            val sp = SPUtils.getInstance(APP_SETTINGS_KEY)
            with(binding) {
                if (eventCheckbox.isChecked) {
                    sp.put("marketingAgree", true)
                    sp.put("receiveAppAlert", appNotificationTextView.isSelected)
                    sp.put("receiveSmsAlert", messageNotificationTextView.isSelected)
                }
            }
            findNavController().navigate(R.id.action_serviceAgreeFragment_to_addressSetFragment)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 체크박스가 모두 on일때 전체 선택의 체크도 on으로 바꿔주는 함수
     */
    private fun allCheckBoxOn() {
        var checkBoxCounter = 0
        for (index: Int in 0 until joinAgreeItem.size) {
            if (joinAgreeItem[index].checkBoxSwitchItem.isChecked) ++checkBoxCounter
        }
        if (checkBoxCounter == joinAgreeItem.size){ binding.allCheckBox.isChecked = true}
    }

    /**
     * 이벤트 체크박스의 하위 체크박스가 on일때 on으로 바꿔주는 함수
     */
    private fun eventCheckboxOn() {
        with(binding) {
            if (appNotificationTextView.currentTextColor == ColorUtils.getColor(R.color.mainColor) &&
                messageNotificationTextView.currentTextColor == ColorUtils.getColor(R.color.mainColor)
            ) {
                eventCheckbox.isChecked = true
            }
        }
    }

    /**
     * 체크박스 클릭시 글자색 변경
     */
    private fun checkBoxColorSwitch(checked: Boolean, place: TextView) {
        with(place) {
            if (checked) setTextColor(ColorUtils.getColor(R.color.mainColor))
            else setTextColor(ColorUtils.getColor(R.color.agreeDisableColor))
        }
    }

    /**
     * 체크박스가 없는 레이아웃 글자색 변경
     */
    private fun textViewColorSwitch(place: TextView) {
        with(place) {
            if (currentTextColor == ColorUtils.getColor(R.color.mainColor))
                setTextColor(ColorUtils.getColor(R.color.agreeDisableColor))
            else setTextColor(ColorUtils.getColor(R.color.mainColor))
        }
    }

    private fun eventCheckBoxOn() {
        with(binding) {
            if (appNotificationTextView.isSelected && messageNotificationTextView.isSelected) eventCheckbox.isChecked =
                true
        }
    }

    private fun TextView.changeTextColorBySelectState() {
        val newColor =
            ColorUtils.getColor(if (isSelected) R.color.mainColor else R.color.agreeDisableColor)
        setTextColor(newColor)
    }

    /**
     * 해당 항목의 체크박스의 체크상태에 따라 그 항목에 해당하는 문구의 색상을 변경한다.
     */
    private fun TextView.changeTextColorByCheckBoxState(checked: Boolean) {
        val newColor =
            ColorUtils.getColor(if (checked) R.color.mainColor else R.color.agreeDisableColor)
        setTextColor(newColor)
    }

    /**
     * 다음 버튼에 대한 설정을 수행한다.
     * 다음으로 넘어갈 수 있는지 체크하고 다음 버튼을 활성화한다.
     */
    private fun settingsForNextButton() {
        with(binding) {
            val checked = joinAgreeItem[0]
            nextButton.isEnabled = checked.checkBoxSwitchItem.isChecked
        }
    }

    override fun onPause() {
        super.onPause()
        joinAgreeItem.clear()
    }
}