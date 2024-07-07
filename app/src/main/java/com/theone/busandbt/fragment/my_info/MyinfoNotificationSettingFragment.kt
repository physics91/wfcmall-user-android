package com.theone.busandbt.fragment.my_info

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentMyinfoNotificationSettingBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.utils.APP_SETTINGS_KEY

/**
 * my동백 알림설정
 */
class MyinfoNotificationSettingFragment :
    DataBindingFragment<FragmentMyinfoNotificationSettingBinding>(), EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_myinfo_notification_setting
    override val actionBarTitle: String = "알림 설정"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            init()

            //마케팅 활용 동의 체크 시 개인정보 수집 이용 동의 체크박스 모두 on
            alertToggleButton.setOnCheckedChangeListener { _, _ ->
                val sp = SPUtils.getInstance(APP_SETTINGS_KEY)
                if (alertToggleButton.isChecked) {
                    soundCheckBox.isChecked = true
                    vibrateCheckBox.isChecked = true
                }
                if (!alertToggleButton.isChecked && soundCheckBox.isChecked && vibrateCheckBox.isChecked) {
                    soundCheckBox.isChecked = false
                    vibrateCheckBox.isChecked = false
                }
                sp.put("useAlert", alertToggleButton.isChecked)
                sp.put("useSoundAlert", soundCheckBox.isChecked)
                sp.put("useVibrateAlert", vibrateCheckBox.isChecked)
            }

            //개인정보 수집 이용 동의 체크박스 하나라도 off시 마케팅 활용 동의 off
            soundCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked) alertToggleButton.isChecked = false
                if (isChecked && vibrateCheckBox.isChecked) {
                    alertToggleButton.isChecked = true
                }
                val sp = SPUtils.getInstance(APP_SETTINGS_KEY)
                sp.put("useAlert", alertToggleButton.isChecked)
                sp.put("useSoundAlert", isChecked)
            }
            vibrateCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked) alertToggleButton.isChecked = false
                if (isChecked && soundCheckBox.isChecked) {
                    alertToggleButton.isChecked = true
                }
                val sp = SPUtils.getInstance(APP_SETTINGS_KEY)
                sp.put("useAlert", alertToggleButton.isChecked)
                sp.put("useVibrateAlert", isChecked)
            }
        }
    }

    private fun init() {
        with(binding) {
            val sp = SPUtils.getInstance(APP_SETTINGS_KEY)
            alertToggleButton.isChecked = sp.getBoolean("useAlert", true)
            soundCheckBox.isChecked = sp.getBoolean("useSoundAlert", true)
            vibrateCheckBox.isChecked = sp.getBoolean("useVibrateAlert", true)
        }
    }
}