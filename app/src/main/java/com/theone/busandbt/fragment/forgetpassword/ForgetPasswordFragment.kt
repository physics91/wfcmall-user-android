package com.theone.busandbt.fragment.forgetpassword

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.databinding.FragmentForgetPasswordBinding
import com.theone.busandbt.dto.login.request.NormalLoginRequest
import com.theone.busandbt.dto.login.request.SetPasswordForcedRequest
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.fragment.join.BaseJoinFragment
import org.koin.android.ext.android.inject
import java.util.regex.Pattern

/**
 * 비밀번호 설정 페이지입니다.
 */
class ForgetPasswordFragment : BaseJoinFragment<FragmentForgetPasswordBinding>() {
    override val layoutId: Int = R.layout.fragment_forget_password
    override val actionBarTitle: String = EMPTY_ACTION_BAR_TITLE
    private var firstPasswordStatus = false
    private var secondPasswordStatus = false
    private val loginAPI: LoginAPI by inject()

    //숫자
    val numberPattern =
        "^[0-9]*$"

    //영문
    val wordPattern =
        "^[a-zA-Z]*$"

    //특수문자
    val specialPattern =
        "^[$@$!%*#?&.]*$"

    //숫자&영문
    val numberAndWord =
        "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z[0-9]]*$"

    //숫자&특수문자
    val numberAndSpecial =
        "^(?=.*[0-9])(?=.*[$@$!%*#?&.])[[0-9]$@$!%*#?&.]*$"

    //영문&특수문자
    val wordAndSpecial =
        "^(?=.*[A-Za-z])(?=.*[$@$!%*#?&.])[A-Za-z$@$!%*#?&.]*$"

    //숫자&영문&특수문자
    val allPattern =
        "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]*$"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val ctx = requireContext()
            //힌트가 나올시 글자의 폰트와 커서위치 변경
            //   ViewChangeUtils.eventRegistrationHintTextFontDifferenceTextFont(passwordInputEditText, ctx)
            //   ViewChangeUtils.eventRegistrationHintTextFontDifferenceTextFont(passwordReInputEditText, ctx)

            //버튼 클릭시 버튼모양 교체, boolean상태 바꾸면서 함수 호출
            passwordToggleBackButton.setOnClickListener {
                passwordToggleImageView.isSelected = passwordToggleImageView.isSelected != true
                firstPasswordStatus = !firstPasswordStatus
                showPassword(firstPasswordStatus, passwordInputEditText)
            }
            rePasswordToggleBackButton.setOnClickListener {
                rePasswordToggleImageView.isSelected = rePasswordToggleImageView.isSelected != true
                secondPasswordStatus = !secondPasswordStatus
                showPassword(secondPasswordStatus, passwordReInputEditText)
            }

            nextButton.setOnClickListener {
                val token = joinInfoViewModel.tempToken ?: return@setOnClickListener
                val memberId = joinInfoViewModel.memberId ?: return@setOnClickListener
                val loginId = joinInfoViewModel.infoId.value ?: return@setOnClickListener
                val password = passwordInputEditText.text.toString()
                safeApiRequest(
                    loginAPI.setPasswordForced(
                        "Bearer $token",
                        memberId,
                        SetPasswordForcedRequest(
                            loginId,
                            password
                        )
                    )
                ) {
                    showMessageDialog("비밀번호 재설정이 완료되었습니다.") {
                        onDoneButtonClick {
                            safeApiRequest(
                                loginAPI.login(
                                    NormalLoginRequest(
                                        loginId = loginId,
                                        loginPassword = password
                                    )
                                )
                            ) {
                                dismiss()
                                loginInfoViewModel.loginSuccess(it.userId, it.token) {
                                    val navController =
                                        this@ForgetPasswordFragment.findNavController()
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }

            //비밀번호 작성시 조건확인 이벤트, 비밀번호 조건이 맞을 시 버튼 활성화 함수또한 실행
            passwordInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(passwordCheck: Editable?) {
                    val checkText = passwordInputEditText.text?.toString() ?: return
                    if (checkText.isEmpty()) {
                        passwordStatusTextView.isVisible = false
                        return
                    }
                    when {
                        Pattern.matches(numberPattern, checkText) &&
                                !Pattern.matches(numberAndWord, checkText) &&
                                !Pattern.matches(numberAndSpecial, checkText) -> {
                            passwordStatusTextView.text = "영문/특수문자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible =
                                passwordReInputEditText.text?.isNotEmpty() == true
                            buttonConverter()
                        }

                        Pattern.matches(wordPattern, checkText) &&
                                !Pattern.matches(wordAndSpecial, checkText) &&
                                !Pattern.matches(numberAndWord, checkText) -> {
                            passwordStatusTextView.text = "숫자/특수문자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible =
                                passwordReInputEditText.text?.isNotEmpty() == true
                            buttonConverter()
                        }

                        Pattern.matches(specialPattern, checkText) &&
                                !Pattern.matches(numberAndSpecial, checkText) &&
                                !Pattern.matches(wordAndSpecial, checkText) -> {
                            passwordStatusTextView.text = "영문/숫자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible =
                                passwordReInputEditText.text?.isNotEmpty() == true
                            buttonConverter()
                        }

                        Pattern.matches(numberAndWord, checkText) -> {
                            passwordStatusTextView.text = "특수문자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible =
                                passwordReInputEditText.text?.isNotEmpty() == true
                            buttonConverter()
                        }

                        Pattern.matches(numberAndSpecial, checkText) -> {
                            passwordStatusTextView.text = "영문이 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible =
                                passwordReInputEditText.text?.isNotEmpty() == true
                            buttonConverter()
                        }

                        Pattern.matches(wordAndSpecial, checkText) -> {
                            passwordStatusTextView.text = "숫자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible =
                                passwordReInputEditText.text?.isNotEmpty() == true
                            buttonConverter()
                        }

                        Pattern.matches(
                            allPattern,
                            checkText
                        ) && checkText.length < 8 -> {
                            passwordStatusTextView.text = "8자 이상 조건이 맞지 않습니다."
                            passwordStatusTextView.isVisible =
                                passwordReInputEditText.text?.isNotEmpty() == true
                            buttonConverter()
                        }

                        Pattern.matches(
                            allPattern,
                            checkText
                        ) && checkText.length >= 8 -> {
                            passwordStatusTextView.text = "완료"
                            passwordStatusTextView.isVisible = false
                            buttonConverter()
                        }

                        else -> {
                            passwordStatusTextView.text = null
                            passwordStatusTextView.isVisible = false
                        }
                    }
                }
            })

            //비밀번호 일치 여부 확인
            passwordReInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(edit: Editable?) {
                    if (passwordReInputEditText.text?.isNotEmpty() == true) {
                        val flag =
                            passwordInputEditText.text.toString() == passwordReInputEditText.text.toString()
                        matchStatusTextView.text = if (flag) "완료" else "비밀번호가 일치하지 않습니다."
                        matchStatusTextView.isVisible = !flag
                        buttonConverter()
                    }
                }
            })
        }
    }

    // 비밀번호 표시&숨기기 함수
    private fun showPassword(isShow: Boolean, place: EditText) {
        with(binding) {
            place.transformationMethod =
                if (isShow) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            //표시&숨기기 설정시 커서가 초기위치로 돌아감을 방지
            passwordInputEditText.setSelection(passwordInputEditText.text.toString().length)
            passwordReInputEditText.setSelection(passwordReInputEditText.text.toString().length)
        }
    }

    private fun buttonConverter() {
        with(binding) {
            nextButton.isEnabled = (passwordStatusTextView.text == matchStatusTextView.text)
        }
    }
}