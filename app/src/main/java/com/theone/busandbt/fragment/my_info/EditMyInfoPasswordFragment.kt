package com.theone.busandbt.fragment.my_info

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
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.databinding.FragmentEditMyInfoPasswordBinding
import com.theone.busandbt.dto.login.request.SetPasswordRequest
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import org.koin.android.ext.android.inject
import java.util.regex.Pattern

/**
 * 비밀번호 변경 프래그먼트
 * 비밀번호는 조건에 맞지 않을시 오류 메시지를 입력창 아래에 출력
 */
class EditMyInfoPasswordFragment : DataBindingFragment<FragmentEditMyInfoPasswordBinding>(),
    EnabledGoBackButton, RequiredLogin {
    override val layoutId: Int = R.layout.fragment_edit_my_info_password
    override val actionBarTitle: String = "비밀번호 변경"
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
            //입력창의 패스워드 보기 기능 추가. 클릭 전 모든 입력창을 숨기고 클릭 이벤트를 추가한다.
            showPassword(currentEditToggle.isChecked, currentEdit)
            currentEditToggle.setOnClickListener {
                showPassword(
                    currentEditToggle.isChecked,
                    currentEdit
                )
            }
            showPassword(newEditToggle.isChecked, newEdit)
            newEditToggle.setOnClickListener {
                showPassword(
                    newEditToggle.isChecked,
                    newEdit
                )
            }
            showPassword(confirmEditToggle.isChecked, confirmEdit)
            confirmEditToggle.setOnClickListener {
                showPassword(
                    confirmEditToggle.isChecked,
                    confirmEdit
                )
            }
            //새 비밀번호 조건 확인
            passwordCondition()
            //새 비밀번호 일치여부 확인
            passwordConfirmStatus()

            changePasswordBtn.setOnClickListener {
                val currentPassword = currentEdit.text.toString()
                if (currentPassword.isEmpty()) return@setOnClickListener
                val newPassword = confirmEdit.text.toString()
                if (newPassword.isEmpty()) return@setOnClickListener
                val li = loginInfo
                safeApiRequest(
                    loginAPI.setPassword(
                        token = li?.getFormedToken() ?: return@setOnClickListener,
                        userId = li.id,
                        SetPasswordRequest(
                            li.tel,
                            currentPassword,
                            newPassword
                        )
                    ),
                    onFail = { _, _ ->
                        currentState.isVisible = true
                    }
                ) {
                    view.showMessageBar("비밀번호가 정상적으로 변경되었어요.")
                    findNavController().popBackStack()
                }
            }
        }
    }

    // 비밀번호 표시&숨기기 함수
    private fun showPassword(isShow: Boolean, place: EditText) {
        if (isShow) {
            place.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            place.transformationMethod = PasswordTransformationMethod.getInstance()
        }
        //표시&숨기기 설정시 커서가 초기위치로 돌아감을 방지
        place.setSelection(place.text.toString().length)
    }

    //새 비밀번호 일치 확인 함수
    private fun passwordConfirmStatus() {
        with(binding) {
            confirmEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(edit: Editable?) {
                    if (confirmEdit.text.toString() == newEdit.text.toString() || confirmEdit.text.toString().isEmpty()) {
                        confirmState.text = "완료"
                        buttonConverter()
                        confirmState.visibility = View.GONE
                    } else {
                        confirmState.text = "비밀번호가 일치하지 않습니다."
                        buttonConverter()
                        confirmState.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    //새 비밀번호 조건 확인 함수
    private fun passwordCondition() {
        with(binding) {
            newEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(passwordCheck: Editable?) {
                    when {
                        (newEdit.text?.length == 0) -> {
                            newState.visibility = View.GONE
                        }
                        (Pattern.matches(numberPattern, newEdit.text) &&
                                !Pattern.matches(numberAndWord, newEdit.text) &&
                                !Pattern.matches(numberAndSpecial, newEdit.text)) -> {
                            newState.text = "영문/특수문자가 포함되지 않았습니다. "
                            newState.visibility = View.VISIBLE
                            buttonConverter()
                        }
                        (Pattern.matches(wordPattern, newEdit.text) &&
                                !Pattern.matches(wordAndSpecial, newEdit.text) &&
                                !Pattern.matches(numberAndWord, newEdit.text)) -> {
                            newState.text = "숫자/특수문자가 포함되지 않았습니다. "
                            newState.visibility = View.VISIBLE
                            buttonConverter()
                        }
                        (Pattern.matches(specialPattern, newEdit.text) &&
                                !Pattern.matches(numberAndSpecial, newEdit.text) &&
                                !Pattern.matches(wordAndSpecial, newEdit.text)) -> {
                            newState.text = "영문/숫자가 포함되지 않았습니다. "
                            newState.visibility = View.VISIBLE
                            buttonConverter()
                        }
                        (Pattern.matches(numberAndWord, newEdit.text)) -> {
                            newState.text = "특수문자가 포함되지 않았습니다. "
                            newState.visibility = View.VISIBLE
                            buttonConverter()
                        }
                        (Pattern.matches(numberAndSpecial, newEdit.text)) -> {
                            newState.text = "영문이 포함되지 않았습니다. "
                            newState.visibility = View.VISIBLE
                            buttonConverter()
                        }
                        (Pattern.matches(wordAndSpecial, newEdit.text)) -> {
                            newState.text = "숫자가 포함되지 않았습니다. "
                            newState.visibility = View.VISIBLE
                            buttonConverter()
                        }
                        (Pattern.matches(
                            allPattern,
                            newEdit.text
                        ) && (newEdit.text?.length!! < 8)) -> {
                            newState.text = "8자 이상 조건이 맞지 않습니다."
                            newState.visibility = View.VISIBLE
                            buttonConverter()
                        }
                        (Pattern.matches(
                            allPattern,
                            newEdit.text
                        ) && (newEdit.text?.length!! >= 8)) -> {
                            newState.text = "완료"
                            newState.visibility = View.GONE
                            buttonConverter()
                        }
                        newEdit.text.toString().isEmpty() -> {
                            newState.text = "미완료"
                            newState.visibility = View.GONE
                            buttonConverter()
                        }
                        else -> {
                            newState.text = null
                            newState.visibility = View.GONE
                        }
                    }
                }
            })
        }
    }

    //버튼 활성화 및 색상 변경 함수
    private fun buttonConverter() {
        with(binding) {
            changePasswordBtn.isEnabled = newState.text == confirmState.text
        }
    }
}
