package com.theone.busandbt.fragment.my_info

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.orderchannel.MemberAPI
import com.theone.busandbt.databinding.FragmentEditMyInfoBinding
import com.theone.busandbt.dto.member.request.SetMemberRequest
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.function.emailTextWatcher
import com.theone.busandbt.function.nameTextWatcher
import com.theone.busandbt.utils.NAME_INPUT_FILTERS
import org.koin.android.ext.android.inject

/**
 * 회원정보 수정 프래그먼트
 * 휴대폰 번호, 이름을 변경하는 기능을 가졌고, 비밀번호 변경 프래그먼트로 이동가능하다.
 */
class EditMyInfoFragment : DataBindingFragment<FragmentEditMyInfoBinding>(), RequiredLogin,
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_edit_my_info
    override val actionBarTitle: String = "회원 정보 수정"
    private val memberApi: MemberAPI by inject()
    private val nameTextWatcher = nameTextWatcher { _, passed ->
        with(binding) {
            nameChangeButton.isEnabled = passed
        }
    }
    private val emailTextWatcher = emailTextWatcher { _, passed ->
        with(binding) {
            emailChangeButton.isEnabled = passed
        }
    }

    override fun onPause() {
        super.onPause()
        with(binding) {
            nameEdit.removeTextChangedListener(nameTextWatcher)
            emailEdit.removeTextChangedListener(emailTextWatcher)
        }
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            nameEdit.addTextChangedListener(nameTextWatcher)
            emailEdit.addTextChangedListener(emailTextWatcher)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            nameEdit.filters = NAME_INPUT_FILTERS
            nameEdit.hint = innerLoginInfo.nickname
            emailEdit.hint = innerLoginInfo.email

            goPasswordChange.setOnClickListener {
                findNavController().navigate(R.id.action_editMyInfoFragment_to_editMyInfoPasswordFragment)
            }

            //입력창 상태에 따라 버튼 조건부 비활성화 & 입력창 활성화
            nameChangeClickEvent()
        }
    }

    private fun nameChangeClickEvent() {
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            nameChangeButton.setOnClickListener {
                if (!nameEdit.isEnabled) {
                    nameChangeButton.text = "완료"
                    nameChangeButton.isEnabled = false
                    nameEdit.isEnabled = true
                    nameEdit.requestFocus()
                    return@setOnClickListener
                }
                val nickname = nameEdit.text.toString()
                safeApiRequest(
                    memberApi.setMember(
                        innerLoginInfo.getFormedToken(),
                        innerLoginInfo.id,
                        SetMemberRequest(nickname = nickname)
                    )
                ) {
                    loginInfo?.nickname = nickname
                    loginInfoViewModel.update()
                    nameEdit.hint = nickname
                    nameEdit.text = null
                    nameChangeButton.setBackgroundResource(R.drawable.bg_change_text_btn_selector)
                    nameChangeButton.isEnabled = true
                    nameEdit.isEnabled = false
                    nameChangeButton.showMessageBar("닉네임이 정상적으로 변경되었어요.")
                }
            }

            emailChangeButton.setOnClickListener {
                if (!emailEdit.isEnabled) {
                    emailChangeButton.text = "완료"
                    emailChangeButton.isEnabled = false
                    emailEdit.isEnabled = true
                    emailEdit.requestFocus()
                    return@setOnClickListener
                }
                val email = emailEdit.text.toString()
                //입력창 비활성화 시 입력창 색상변경, 힌트 재설정, 입력값 초기화
                safeApiRequest(
                    memberApi.setMember(
                        innerLoginInfo.getFormedToken(),
                        innerLoginInfo.id,
                        SetMemberRequest(email = email)
                    )
                ) {
                    loginInfo?.email = email
                    emailEdit.hint = email
                    emailEdit.text = null
                    emailChangeButton.text = "변경"
                    emailChangeButton.isEnabled = true
                    emailEdit.isEnabled = false
                    loginInfoViewModel.update()
                    view?.showMessageBar("이메일이 정상적으로 변경되었어요.")
                }
            }
        }
    }
}