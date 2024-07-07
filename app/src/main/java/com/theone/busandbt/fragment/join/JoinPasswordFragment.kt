package com.theone.busandbt.fragment.join

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
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.databinding.FragmentJoinPasswordBinding
import com.theone.busandbt.dto.login.request.JoinRequest
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.utils.LOCAL_DATE_FORMATTER
import org.koin.android.ext.android.inject
import java.util.regex.Pattern

/**
 * 비밀번호 설정 페이지입니다.
 */
class JoinPasswordFragment : BaseJoinFragment<FragmentJoinPasswordBinding>(), EnabledGoBackButton,
    View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_join_password
    override val actionBarTitle: String = EMPTY_ACTION_BAR_TITLE
    private val loginAPI: LoginAPI by inject()

    private var firstPasswordStatus = false
    private var secondPasswordStatus = false

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

            joinInfoViewModel.infoPassword.observe(viewLifecycleOwner) {
                passwordInputEditText.setText(it)
            }

            //버튼 클릭시 버튼모양 교체, boolean상태 바꾸면서 함수 호출
            passwordToggleBackButton.setOnClickListener(this@JoinPasswordFragment)
            rePasswordToggleBackButton.setOnClickListener(this@JoinPasswordFragment)
            nextButton.setOnClickListener(this@JoinPasswordFragment)

            eventRegistrationPasswordButtonStatusChange()
            eventRegistrationPasswordEditText()
            eventRegistrationEmailEditText()
        }
    }

    /**
     *  비밀번호  eidtText를 addTextChangedListener 이벤트에 등록하여
     *  비밀번호 정규식을 확안하고 그에 따라 상태메시지에 들어갈 텍스트 작성 및 isVisble 상태 변경
     */
    private fun eventRegistrationPasswordButtonStatusChange() {
        with(binding) {
            //비밀번호 작성시 조건확인 이벤트, 비밀번호 조건이 맞을 시 버튼 활성화 함수또한 실행
            passwordInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(passwordCheck: Editable?) {
                    when {
                        (Pattern.matches(numberPattern, passwordInputEditText.text) &&
                                !Pattern.matches(
                                    numberAndWord,
                                    passwordInputEditText.text
                                ) &&
                                !Pattern.matches(
                                    numberAndSpecial,
                                    passwordInputEditText.text
                                )) -> {
                            passwordStatusTextView.text = "영문/특수문자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible= passwordReInputEditText.text?.isNotEmpty() == true
                            buttonConverter()
                        }
                        (Pattern.matches(wordPattern, passwordInputEditText.text) &&
                                !Pattern.matches(
                                    wordAndSpecial,
                                    passwordInputEditText.text
                                ) &&
                                !Pattern.matches(
                                    numberAndWord,
                                    passwordInputEditText.text
                                )) -> {
                            passwordStatusTextView.text = "숫자/특수문자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible = true
                            buttonConverter()
                        }
                        (Pattern.matches(specialPattern, passwordInputEditText.text) &&
                                !Pattern.matches(
                                    numberAndSpecial,
                                    passwordInputEditText.text
                                ) &&
                                !Pattern.matches(
                                    wordAndSpecial,
                                    passwordInputEditText.text
                                )) -> {
                            passwordStatusTextView.text = "영문/숫자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible = true
                            buttonConverter()
                        }
                        (Pattern.matches(numberAndWord, passwordInputEditText.text)) -> {
                            passwordStatusTextView.text = "특수문자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible = true
                            buttonConverter()
                        }
                        (Pattern.matches(numberAndSpecial, passwordInputEditText.text)) -> {
                            passwordStatusTextView.text = "영문이 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible = true
                            buttonConverter()
                        }
                        (Pattern.matches(wordAndSpecial, passwordInputEditText.text)) -> {
                            passwordStatusTextView.text = "숫자가 포함되지 않았습니다. "
                            passwordStatusTextView.isVisible = true
                            buttonConverter()
                        }
                        (Pattern.matches(
                            allPattern,
                            passwordInputEditText.text
                        ) && (passwordInputEditText.text?.length!! < 8)) -> {
                            passwordStatusTextView.text = "8자 이상 조건이 맞지 않습니다."
                            passwordStatusTextView.isVisible = true
                            buttonConverter()
                        }
                        (Pattern.matches(
                            allPattern,
                            passwordInputEditText.text
                        ) && (passwordInputEditText.text?.length!! >= 8)) -> {
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
        }
    }

    /**
     * 비밀번호 재입력 eidtText를 addTextChangedListener 이벤트에 등록하여
     * 비밀번호 와 비밀번호 재입력의 텍스트를 확인한다.
     */
    private fun eventRegistrationPasswordEditText() {
        with(binding) {
            passwordReInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(edit: Editable?) {
                    if(passwordReInputEditText.text?.isNotEmpty() == true) {
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

    /**
     * 이메일 eidtText를 addTextChangedListener 이벤트에 등록하여
     * 이메일의 정규식을 확인한다
     */
    private fun eventRegistrationEmailEditText() {
        with(binding) {
            emailEditTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(edit: Editable?) {
                    val flag =
                        Pattern.matches(
                            "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}\$",
                            emailEditTextView.text
                        ) || emailEditTextView.text?.isEmpty() == true
                    emailStatusTextView.text = if (flag) "완료" else "이메일 형식이 맞지 않습니다."
                    emailStatusTextView.isVisible = !flag
                    buttonConverter()
                }
            })
        }
    }

    // 비밀번호 표시&숨기기 함수
    private fun showPassword(isShow: Boolean, place: EditText) {
        with(binding) {
            place.transformationMethod =
                if (isShow) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            passwordInputEditText.setSelection(passwordInputEditText.text.toString().length)
            passwordReInputEditText.setSelection(passwordReInputEditText.text.toString().length)
        }
    }

    private fun buttonConverter() {
        with(binding) {
            nextButton.isEnabled = (passwordStatusTextView.text == matchStatusTextView.text) && (passwordStatusTextView.text == emailStatusTextView.text)
        }
    }

    override fun onClick(view: View?) {
        with(binding) {
            when (view) {
                passwordToggleBackButton -> {
                    passwordToggleImageView.isSelected = passwordToggleImageView.isSelected != true
                    firstPasswordStatus = !firstPasswordStatus
                    showPassword(firstPasswordStatus, passwordInputEditText)
                }
                rePasswordToggleBackButton -> {
                    rePasswordToggleImageView.isSelected =
                        rePasswordToggleImageView.isSelected != true
                    secondPasswordStatus = !secondPasswordStatus
                    showPassword(secondPasswordStatus, passwordReInputEditText)
                }
                nextButton -> {
                    val email = emailEditTextView.text?.toString() ?: ""
                    val loginId = joinInfoViewModel.infoId.value!!
                    joinInfoViewModel.setLoginPassword(passwordInputEditText.text.toString())
                    joinInfoViewModel.setEmail(email)
                    safeApiRequest(
                        loginAPI.join(
                            JoinRequest(
                                loginId = loginId,
                                loginPassword = passwordInputEditText.text.toString(),
                                name = joinInfoViewModel.nameLiveData.value!!,
                                nickname = joinInfoViewModel.nicknameLiveData.value!!,
                                telecomType = joinInfoViewModel.telecomTypeLiveData.value!!.id,
                                tel = loginId,
                                birth = LOCAL_DATE_FORMATTER.format(joinInfoViewModel.birthLiveData.value!!),
                                email = email,
                                gender = joinInfoViewModel.genderLiveData.value!!.id
                            )
                        )
                    ) {
                        loginInfoViewModel.loginSuccess(it.userId, it.token) {
                            findNavController().navigate(
                                JoinPasswordFragmentDirections.actionJoinPasswordFragmentToMainFragment(
                                    true
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}