package com.theone.busandbt.fragment.my_info

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentMyinfoAccountSettingBinding
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.utils.APP_SETTINGS_KEY

/**
 * 내정보 계정설정 화면
 */
class MyinfoAccountSettingFragment : DataBindingFragment<FragmentMyinfoAccountSettingBinding>(), EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_myinfo_account_setting
    override val actionBarTitle: String = "계정 설정"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            init()
            // 현재 스위치는 체크될시 토스트문 출력
            securityTelToggleButton.setOnClickListener {
                SPUtils.getInstance(APP_SETTINGS_KEY).put("useSecurityTel", securityTelToggleButton.isChecked)
            }
            orderCountToggleButton.setOnClickListener {
                SPUtils.getInstance(APP_SETTINGS_KEY).put("sendOrderCount", orderCountToggleButton.isChecked)
            }
        }
    }

    private fun init() {
        with(binding) {
            val sp = SPUtils.getInstance(APP_SETTINGS_KEY)
            val useSecurityTelFlag = sp.getBoolean("useSecurityTel", true)
            val sendOrderCountFlag = sp.getBoolean("sendOrderCount")
            securityTelToggleButton.isChecked = useSecurityTelFlag
            orderCountToggleButton.isChecked = sendOrderCountFlag
        }
    }
}