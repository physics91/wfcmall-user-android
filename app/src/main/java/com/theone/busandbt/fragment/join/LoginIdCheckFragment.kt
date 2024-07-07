package com.theone.busandbt.fragment.join

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.databinding.FragmentJoinPhoneNoCheckBinding
import com.theone.busandbt.dto.login.request.CheckLoginIdRequest
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.type.Nice24ActionType
import com.busandbt.code.JoinType
import com.theone.busandbt.extension.safeApiRequest
import org.koin.android.ext.android.inject


/**
 * 회원가입 중복 방지를 위한 휴대폰 번호 입력하는 화면
 */
class LoginIdCheckFragment : BaseJoinFragment<FragmentJoinPhoneNoCheckBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_join_phone_no_check
    override val actionBarTitle: String = EMPTY_ACTION_BAR_TITLE
    private val loginAPI: LoginAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            joinInfoViewModel.infoId.observe(viewLifecycleOwner) {
                loginIdEditText.setText(it)
            }

            loginIdEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(frontNum: Editable?) {
                    nextButton.isEnabled = loginIdEditText.editableText.length == 11
                }
            })

            nextButton.setOnClickListener {
                val loginId = loginIdEditText.text.toString()
                safeApiRequest(
                    loginAPI.checkLoginId(CheckLoginIdRequest(loginId))
                ) {
                    if (it.testResult) {
                        showMessageDialog("이미 가입된 사용자입니다.", showWarningImageView = true) {
                            onDoneButtonClick { dismiss() }
                        }
                        return@safeApiRequest
                    }
                    joinInfoViewModel.setLoginId(loginIdEditText.text.toString())
                    when (joinInfoViewModel.joinType) {
                        JoinType.NORMAL -> {
                            findNavController().navigate(
                                LoginIdCheckFragmentDirections.actionLoginIdCheckFragmentToNice24Fragment(
                                    Nice24ActionType.NORMAL_JOIN.id
                                )
                            )
                        }
                        JoinType.GOOGLE, JoinType.NAVER -> findNavController().navigate(
                            R.id.action_loginIdCheckFragment_to_joinNameFragment
                        )
                        JoinType.KAKAO -> findNavController().navigate(
                            R.id.action_loginIdCheckFragment_to_joinNameFragment
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}