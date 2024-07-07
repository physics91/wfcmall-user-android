package com.theone.busandbt.fragment.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ColorUtils
import com.busandbt.code.JoinType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.api.orderchannel.MemberAPI
import com.theone.busandbt.databinding.FragmentLoginBinding
import com.theone.busandbt.dto.login.request.NormalLoginRequest
import com.theone.busandbt.dto.login.request.SNSLoginRequest
import com.theone.busandbt.dto.member.request.CheckSnsMemberRequest
import com.theone.busandbt.extension.errorLog
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.model.JoinInfoViewModel
import com.theone.busandbt.spanned.TypefaceSpanCompat
import com.theone.busandbt.type.Nice24ActionType
import com.theone.busandbt.utils.ONLY_NUMBER_INPUT_FILTERS
import com.theone.busandbt.utils.UnitConverter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.LocalDate

/**
 * 로그인 화면
 * TODO assert 처리는 모두 예외처리나 엘비스 처리를 할 것
 * TODO - 네이버 로그인 검수 받아야함
 * TODO - 카카오톡, 구글 로그인 한산 -> 동백통 쪽으로 변경해야함
 */
class LoginFragment : DataBindingFragment<FragmentLoginBinding>(), EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_login
    override val actionBarTitle: String = EMPTY_ACTION_BAR_TITLE
    private val joinInfoViewModel: JoinInfoViewModel by activityViewModel()
    private val memberAPI: MemberAPI by inject()
    private var showPasswordStatus = false
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val loginAPI: LoginAPI by inject()
    private var googleLoginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == -1) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                getGoogleInfo(task)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            joinInfoViewModel.joinType = JoinType.NORMAL
            idEditTextView.filters = ONLY_NUMBER_INPUT_FILTERS
            initGoogleLogin()
            initKakaoLogin()
            initNaverLogin()
            //힌트가 나올시 글자의 폰트 변경
            changeFont(idEditTextView)
            changeFont(passwordEditTextView)
            //비밀번호 표시/숨기기 버튼
            toggleButton.setOnClickListener {
                toggleButton.isSelected = toggleButton.isSelected != true
                showPasswordStatus = !showPasswordStatus
                togglePasswordVisibility(showPasswordStatus)
            }
            titleTextView.text = buildSpannedString {
                append("로그인하고\n")
                inSpans(
                    TypefaceSpanCompat(
                        ResourcesCompat.getFont(requireContext(), R.font.sult_semibold)
                            ?: return@with
                    ),
                    ForegroundColorSpan(ColorUtils.getColor(R.color.mainColor))
                ) {
                    append("동백통 서비스")
                }
                append("를\n이용해 보세요")
            }
            googleLoginButton.setOnClickListener {
                val client = mGoogleSignInClient ?: return@setOnClickListener
                client.signOut().addOnCompleteListener {
                    val signInIntent = client.signInIntent
                    googleLoginLauncher.launch(signInIntent)
                }
            }
            loginButton.setOnClickListener {
                normalLoginRetrofit()
            }
            naverLoginButton.setOnClickListener {
                startNaverLogin()
            }
            goToJoinTextView.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_joinFragment)
            }
            passwordFindTextView.setOnClickListener {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToNice24Fragment(
                        Nice24ActionType.FIND_PASSWORD.id
                    )
                )
            }
            kakaoLoginButton.setOnClickListener {
                kakaoLogin()
            }
        }
    }

    private fun initNaverLogin() {
        // Naver Login Module Initialize
        val naverClientId = getString(R.string.social_login_info_naver_client_id)
        val naverClientSecret = getString(R.string.social_login_info_naver_client_secret)
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(
            requireContext(),
            naverClientId,
            naverClientSecret,
            naverClientName
        )
        NaverIdLoginSDK.showDevelopersLog(true)
    }

    private fun initKakaoLogin() {

    }

    private fun initGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    //authenticate() 메서드를 네이버 이용한 로그인
    private fun startNaverLogin() {
        var naverToken: String? = ""
        val oauthLoginCallback = object :
            OAuthLoginCallback { // OAuthLoginCallback을 authenticate() 메서드 호출 시 파라미터로 전달하거나 NidOAuthLoginButton 객체에 등록하면 인증이 종료되는 것을 확인할 수 있습니다.
            override fun onSuccess() {
                // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
                naverToken = NaverIdLoginSDK.getAccessToken()
                NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
                    override fun onSuccess(result: NidProfileResponse) {
                        val userId = result.profile?.id.toString()
                        val actualEmail = result.profile?.email ?: ""
                        joinInfoViewModel.setEmail(actualEmail)
                        snsLogin(
                            JoinType.NAVER,
                            userId,
                            actualEmail.split("@").takeIf { it.size == 2 }?.firstOrNull() ?: ""
                        )
                    }

                    override fun onError(errorCode: Int, message: String) {
                    }

                    override fun onFailure(httpStatus: Int, message: String) {
                    }
                })
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(
                    requireContext(), "errorCode: ${errorCode}\n" +
                            "errorDescription: $errorDescription", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }
        NaverIdLoginSDK.authenticate(requireContext(), oauthLoginCallback)
    }

    //구글 사용자 정보
    private fun getGoogleInfo(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val snsClientId = account.id!!
            val actualEmail = account.email ?: ""
            joinInfoViewModel.setEmail(actualEmail)
            snsLogin(
                JoinType.GOOGLE,
                snsClientId,
                actualEmail.split("@").takeIf { it.size == 2 }?.firstOrNull() ?: ""
            )
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

    //카카오 사용자 정보 가져오기
    private fun kakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            when {
                error != null -> {}
                user != null -> {
                    val snsClientId = user.id.toString()
                    joinInfoViewModel.setEmail(user.kakaoAccount?.email ?: "")
                    snsLogin(
                        JoinType.KAKAO,
                        snsClientId,
                        user.kakaoAccount?.profile?.nickname ?: ""
                    )
                }

                else -> {}
            }
        }
    }

    private fun snsLogin(joinType: JoinType, snsClientId: String, name: String) {
        safeApiRequest(
            memberAPI.checkSnsMember(CheckSnsMemberRequest(joinType.id, snsClientId))
        ) {
            if (!it.checkResult) {
                joinInfoViewModel.joinType = joinType
                joinInfoViewModel.snsClientId = snsClientId
                joinInfoViewModel.setNickname(name)
                findNavController().navigate(R.id.action_loginFragment_to_joinFragment)
                return@safeApiRequest
            }
            safeApiRequest(
                loginAPI.loginBySNS(
                    SNSLoginRequest(
                        name = "",
                        nickname = "",
                        tel = "",
                        birth = LocalDate.now(),
                        gender = 1,
                        joinType = joinType.id,
                        snsClientId = snsClientId,
                        email = null
                    )
                )
            ) { response ->
                loginInfoViewModel.loginSuccess(response.userId, response.token) {
                    findNavController().popBackStack()
                }
            }
        }
    }


    //카카오 로그인
    private fun kakaoLogin() {
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> errorLog("카카오톡 로그인 에러", error.message)
                token != null -> kakaoUserInfo()
            }
        }
        val ctx = context ?: return
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(ctx)) {
            UserApiClient.instance.loginWithKakaoTalk(ctx) { token, error ->
                when {
                    error != null -> {
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(
                            requireContext(),
                            callback = callback
                        )
                    }

                    token != null -> kakaoUserInfo()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
        }
    }

    //일반 로그인 레트로핏 통신
    private fun normalLoginRetrofit() {
        safeApiRequest(
            loginAPI.login(
                NormalLoginRequest(
                    loginId = binding.idEditTextView.text.toString(),
                    loginPassword = binding.passwordEditTextView.text.toString()
                )
            ),
            onFail = { _, _ ->
                binding.loginErrorTextView.visibility = View.VISIBLE
            }
        ) {
            loginInfoViewModel.loginSuccess(it.userId, it.token) {
                findNavController().popBackStack()
            }
        }
    }

    //text크기가 0이 아닐때 폰트, 커서 위치 재설정
    private fun changeFont(place: EditText) {
        place.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(frontNum: Editable?) {
                when (place.text.isEmpty()) {
                    true -> {
                        place.setPadding(
                            UnitConverter.dpToPx(7),
                            UnitConverter.dpToPx(10), 0,
                            UnitConverter.dpToPx(14)
                        )
                        place.typeface =
                            ResourcesCompat.getFont(requireContext(), R.font.sult_semibold)
                    }

                    false -> {
                        place.setPadding(
                            UnitConverter.dpToPx(9),
                            UnitConverter.dpToPx(10), 0,
                            UnitConverter.dpToPx(14)
                        )
                        place.typeface =
                            ResourcesCompat.getFont(requireContext(), R.font.sult_semibold)
                    }
                }
            }
        })
    }

    /**
     * 비밀번호 입력란에서 비밀번호 숨김/보임 기능
     * @param isShow true일 경우 비밀번호를 보이게 한다. false일 경우에는 숨김
     */
    private fun togglePasswordVisibility(isShow: Boolean) {
        val passwordForm = binding.passwordEditTextView
        when {
            isShow -> {
                passwordForm.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }

            else -> {
                passwordForm.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
        // 표시/숨기기 설정시 커서가 초기위치로 돌아감을 방지
        passwordForm.setSelection(passwordForm.text.toString().length)
    }
}