package com.theone.busandbt.fragment.join

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.busandbt.code.ChannelType
import com.busandbt.code.Gender
import com.busandbt.code.JoinType
import com.busandbt.code.TelecomType
import com.theone.busandbt.BuildConfig
import com.theone.busandbt.R
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.databinding.FragmentNice24Binding
import com.theone.busandbt.dto.ErrorResponse
import com.theone.busandbt.dto.login.request.CheckLoginIdRequest
import com.theone.busandbt.dto.login.request.SNSLoginRequest
import com.theone.busandbt.extension.*
import com.theone.busandbt.type.Nice24ActionType
import com.theone.busandbt.utils.JACKSON_OBJECT_MAPPER
import com.theone.busandbt.utils.JOIN_LIMIT_AGE
import com.theone.busandbt.utils.LOCAL_DATE_FORMATTER
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.MonthDay

class Nice24Fragment : BaseJoinFragment<FragmentNice24Binding>() {
    override val layoutId: Int = R.layout.fragment_nice24
    private val args by navArgs<Nice24FragmentArgs>()
    private val loginAPI: LoginAPI by inject()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            nice24WebView.settings.javaScriptEnabled = true
            nice24WebView.webViewClient = commonWebClient()
            nice24WebView.addJavascriptInterface(Bridge(), "Nice24Cert")
            val requiredPasswordToken = args.actionTypeId == Nice24ActionType.FIND_PASSWORD.id
            nice24WebView.loadUrl("${BuildConfig.LOGIN_CHANNEL_BASE_URL}/cert/nice24?channelType=${ChannelType.DONG_BAEK.id}&requiredPasswordToken=$requiredPasswordToken")
        }
    }

    inner class Bridge {

        @JavascriptInterface
        fun completeCert(
            resultCode: String,
            tel: String,
            birth: String,
            name: String,
            gender: String,
            telecomType: String,
            passwordToken: String?
        ) {
            if (resultCode != "0000") return
            when (args.actionTypeId) {
                Nice24ActionType.FIND_PASSWORD.id -> {
                    safeApiRequest(
                        loginAPI.checkLoginId(CheckLoginIdRequest(tel))
                    ) {
                        if (!it.testResult) {
                            showMessageDialog("가입되지 않은 사용자입니다.", showWarningImageView = true) {
                                onDoneButtonClick {
                                    dismiss()
                                    this@Nice24Fragment.findNavController().popBackStack()
                                }
                            }
                            return@safeApiRequest
                        }
                        joinInfoViewModel.tempToken = passwordToken
                        joinInfoViewModel.memberId = it.userId
                        joinInfoViewModel.setLoginId(tel)
                        findNavController().navigate(R.id.action_nice24Fragment_to_forgetPasswordFragment)
                    }
                }

                Nice24ActionType.NORMAL_JOIN.id, Nice24ActionType.SNS_JOIN.id -> {
                    val actualBirth =
                        if (birth.isNotEmpty()) LocalDate.from(LOCAL_DATE_FORMATTER.parse(birth)) else LocalDate.MIN
                    val now = LocalDate.now()
                    val target = MonthDay.from(actualBirth)
                    var age = now.year - actualBirth.year
                    // 생일 전일 경우
                    if (target.isAfter(MonthDay.now())) age--
                    if (age < JOIN_LIMIT_AGE) {
                        showMessageDialog(
                            "만 14세 미만의 회원은 가입이 불가합니다.",
                            showWarningImageView = true
                        ) {
                            onDoneButtonClick { goPrev() }
                        }
                        return
                    }
                    when (joinInfoViewModel.joinType) {
                        JoinType.NORMAL -> {
                            safeApiRequest(
                                loginAPI.checkLoginId(CheckLoginIdRequest(tel))
                            ) {
                                if (it.testResult) {
                                    showMessageDialog(
                                        "이미 가입된 사용자입니다.",
                                        showWarningImageView = true
                                    ) {
                                        onDoneButtonClick { goPrev() }
                                    }
                                    return@safeApiRequest
                                }
                                joinInfoViewModel.setLoginId(tel)
                                joinInfoViewModel.setName(name)
                                joinInfoViewModel.setBirth(actualBirth)
                                joinInfoViewModel.setGender(
                                    if (gender != "0") Gender.find(
                                        gender.toInt()
                                    ) else Gender.MAN
                                )
                                joinInfoViewModel.setTelecomType(TelecomType.find(telecomType.toInt()))
                                joinInfoViewModel.certed = true
                                findNavController().navigate(R.id.action_nice24Fragment_to_joinNameFragment)
                            }
                        }

                        else -> {
                            val email = joinInfoViewModel.emailLiveData.value ?: ""
                            safeApiRequest(
                                loginAPI.loginBySNS(
                                    SNSLoginRequest(
                                        name = name,
                                        nickname = email.split("@").takeIf { it.size == 2 }?.firstOrNull() ?: name,
                                        tel = tel,
                                        birth = actualBirth,
                                        gender = if (gender != "0") gender.toInt() else Gender.MAN.id,
                                        joinType = joinInfoViewModel.joinType.id,
                                        snsClientId = joinInfoViewModel.snsClientId ?: "",
                                        email = email,
                                    )
                                ),
                                onFail = { _, rawData ->
                                    if (rawData == null) return@safeApiRequest
                                    try {
                                        val response =
                                            JACKSON_OBJECT_MAPPER.readValue(
                                                rawData,
                                                ErrorResponse::class.java
                                            )
                                        showMessageDialog(
                                            response.message,
                                            showWarningImageView = true
                                        ) {
                                            onDoneButtonClick {
                                                this@Nice24Fragment.view?.findNavController()
                                                    ?.fixedPopBackStack(R.id.loginFragment)
                                                dismiss()
                                            }
                                        }
                                    } catch (_: Throwable) {
                                    }
                                }
                            ) { response ->
                                loginInfoViewModel.loginSuccess(
                                    response.userId,
                                    response.token
                                ) {
                                    findNavController().navigate(
                                        Nice24FragmentDirections.actionNice24FragmentToMainFragment(
                                            true
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun goPrev() {
        joinInfoViewModel.removeLoginId()
        findNavController().popBackStack()
    }
}