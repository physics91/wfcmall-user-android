package com.theone.busandbt.fragment.order

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.blankj.utilcode.util.Utils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.loginchannel.CertAPI
import com.theone.busandbt.api.orderchannel.MemberAPI
import com.theone.busandbt.databinding.FragmentOrderPhoneChangeBinding
import com.theone.busandbt.dto.cert.request.AddCertNumberRequest
import com.theone.busandbt.dto.cert.request.CheckCertNumberRequest
import com.theone.busandbt.dto.member.request.SetMemberRequest
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.utils.ONLY_NUMBER_INPUT_FILTERS
import org.koin.android.ext.android.inject

/**
 * 바로배달 주문하기 휴대폰 변경 프래그먼트
 */
class OrderPhoneChangeFragment : DataBindingFragment<FragmentOrderPhoneChangeBinding>(),
    EnabledGoBackButton, RequiredLogin {
    override val layoutId: Int = R.layout.fragment_order_phone_change
    override val actionBarTitle: String = "휴대폰 번호 변경"
    private val certAPI: CertAPI by inject()
    private val memberAPI: MemberAPI by inject()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            // text의 크기가 0일때 폰트 스타일 재설정
            phoneEdit.filters = ONLY_NUMBER_INPUT_FILTERS
            phoneEdit.filters += LengthFilter(11)
            phoneEdit.hint = innerLoginInfo.tel

            phoneEdit.setOnFocusChangeListener { view, hasFocus ->
                view.setBackgroundResource(if (hasFocus) R.drawable.bg_address_edittext_selector else R.drawable.bg_round_10dp_white)
            }
            certNoEditText.setOnFocusChangeListener { view, hasFocus ->
                view.setBackgroundResource(if (hasFocus) R.drawable.bg_address_edittext_selector else R.drawable.bg_round_10dp_white)
            }

            //입력창 상태에 따라 버튼 조건부 비활성화 & 입력창 활성화
            initTelButton()
            initCertButton()

            //입력값의 길이에 따라 버튼 활성화
            registerPhoneEditListener(phoneEdit)
            registerCertEditListener(certNoEditText)
        }
    }

    private fun initCertButton() {
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            certButton.setOnClickListener {
                val tel = phoneEdit.text.toString()
                val certNumber = certNoEditText.text.toString()
                if (tel.isEmpty() || certNumber.isEmpty()) return@setOnClickListener
                safeApiRequest(
                    certAPI.checkCertNumber(CheckCertNumberRequest(tel, certNumber))
                ) {
                    if (it.testResult) {
                        safeApiRequest(
                            memberAPI.setMember(
                                innerLoginInfo.getFormedToken(),
                                innerLoginInfo.id,
                                SetMemberRequest(
                                    tel = tel
                                )
                            )
                        ) {
                            innerLoginInfo.tel = tel
                            phoneEdit.hint = tel
                            phoneEdit.text = null
                            certStatusTextView.visibility = View.GONE
                            certButton.visibility = View.GONE
                            certNoEditText.visibility = View.GONE
                            phoneChange.setBackgroundResource(R.drawable.bg_change_text_btn_selector)
                            phoneChange.isEnabled = true
                            phoneEdit.isEnabled = false
                            loginInfoViewModel.update()
                            view?.showMessageBar("휴대폰 번호가 정상적으로 변경되었어요.")
                        }
                    } else {
                        // 조건이 틀릴시 xml의 id:certificationStatus 활성화
                        certStatusTextView.isVisible = true
                    }
                }
            }
        }
    }

    // 휴대폰 번호 변경버튼 클릭 시 이벤트
    private fun initTelButton() {
        with(binding) {
            phoneChange.setOnClickListener {
                if (!phoneEdit.isEnabled) {
                    phoneChange.isEnabled = false
                    phoneChange.text = "인증받기"
                    phoneEdit.isEnabled = true
                    phoneEdit.requestFocus()
                    return@setOnClickListener
                }
                val tel = phoneEdit.text.toString()
                if (tel.isEmpty()) {
                    return@setOnClickListener
                }
                if (certNoEditText.visibility != View.GONE) {
                    safeApiRequest(
                        certAPI.addCertNumber(AddCertNumberRequest(tel))
                    ) {
                        view?.showMessageBar("인증번호를 재전송했어요")
                        certNoEditText.requestFocus()
                    }
                    return@setOnClickListener
                }
                safeApiRequest(
                    certAPI.addCertNumber(AddCertNumberRequest(tel))
                ) {
                    view?.showMessageBar("인증번호를 전송했어요")
                    phoneChange.isEnabled = true
                    phoneChange.text = "재발송"
                    certButton.visibility = View.VISIBLE
                    certNoEditText.visibility = View.VISIBLE
                    certNoEditText.requestFocus()
                }
            }
        }
    }


    //폰 버튼 색 변경 함수
    private fun registerPhoneEditListener(phoneEdit: EditText) {
        phoneEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            //텍스트 작성시 hint와 차이를 주기 위해 font 재설정, 이름 작성조건 만족시 버튼 활성화
            override fun afterTextChanged(p0: Editable?) {
                phoneEdit.typeface =
                    ResourcesCompat.getFont(
                        Utils.getApp(),
                        if (phoneEdit.text.isEmpty()) R.font.sult_bold else R.font.sult_medium
                    )
                val telButton = binding.phoneChange
                if (phoneEdit.text.toString().length == 11) {
                    telButton.text = "인증받기"
                    telButton.isEnabled = true
                } else {
                    telButton.text = "인증받기"
                    telButton.isEnabled = false
                }
            }
        })
    }

    private fun registerCertEditListener(edit: TextView) {
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            //텍스트 작성시 hint와 차이를 주기 위해 font 재설정, 이름 작성조건 만족시 버튼 활성화
            override fun afterTextChanged(p0: Editable?) {
                val certChangeForm = binding.certButton
                if (edit.text.toString().length == 6) {
                    certChangeForm.text = "인증완료"
                    certChangeForm.isEnabled = true
                } else {
                    certChangeForm.text = "인증완료"
                    certChangeForm.isEnabled = false
                }
            }
        })
    }
}