package com.theone.busandbt.model

import androidx.lifecycle.*
import com.theone.busandbt.api.orderchannel.MemberAPI
import com.theone.busandbt.extension.callOnSuccess
import com.theone.busandbt.item.LoginInfo
import com.theone.busandbt.repository.MemberInfoRepository
import com.busandbt.code.Gender
import com.busandbt.code.UserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

/**
 * 로그인 정보 공용 데이터 객체
 * 로그인 정보에 대한 전반적인 기능을 다룬다.
 */
class LoginInfoViewModel(private val repository: MemberInfoRepository) : ViewModel() {

    private val memberAPI: MemberAPI by inject(MemberAPI::class.java)
    private val _loginFlagLiveData = MutableLiveData(false)

    /**
     * 로그인 상태값 관찰 인스턴스
     * 값이 true일 경우 로그인이 된 상태이다.
     * 로그인 상태 변경에 따른 처리가 필요할 경우 이 데이터를 Observe한다.
     */
    val loginFlagLiveData: LiveData<Boolean> = _loginFlagLiveData

    /**
     * 로그인 데이터 관찰 인스턴스
     * 로그인 데이터 변경에 따른 처리가 필요할 경우 이 데이터를 Observe한다.
     */
    val loginInfoLiveData: MutableLiveData<LoginInfo?> = MutableLiveData()

    val initLiveData = MutableLiveData<Unit>()

    /**
     * 가장 먼저 실행되어야할 함수
     * 이게 실행되지 않으면 [loginInfoLiveData]에 초기 데이터가 들어가지 않는다. 즉, 기존 로그인 데이터가 있어도 [getLoginInfo]이 null이 반환된다.
     */
    fun init() = viewModelScope.launch(Dispatchers.IO) {
        val loginInfo = repository.getLoginInfo()
        launch(Dispatchers.Main) {
            loginInfoLiveData.value = loginInfo
            _loginFlagLiveData.value = loginInfo != null
            initLiveData.value = Unit
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, op: (loginInfo: LoginInfo?) -> Unit) {
        loginInfoLiveData.observe(lifecycleOwner) {
            op(it)
        }
    }

    /**
     * 회원정보 추가
     * 로그인 성공 시에 정보를 조회해서 여기에 입력해야한다.
     * TODO 비동기로 실행되는거라 동시성 제어가 필요함
     */
    fun setLoginInfo(memberInfo: LoginInfo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(memberInfo)
        launch(Dispatchers.Main) {
            loginInfoLiveData.value = memberInfo
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.IO) {
        val loginInfo = loginInfoLiveData.value ?: return@launch
        repository.update(loginInfo)
    }

    /**
     * 로그인 상태인지 체크한다.
     * @return true일 경우 로그인 상태이다.
     */
    fun isLoginState(): Boolean = loginFlagLiveData.value ?: false

    /**
     * 로컬캐시에서 로그인 정보를 조회한다.
     */
    fun getLoginInfo(): LoginInfo? = loginInfoLiveData.value

    /**
     * 로그인 성공 시 반드시 사용해야한다.
     * @param userId 사용자 고유번호, 회원 앱에선 memberId랑 일맥상통하는 개념
     * @param token 사용자 인증 토큰
     * @param after 로그인 성공 로직 처리 후 실행되어야 할 것들을 정의할 수 있다. 안하면 생략된다.
     */
    fun loginSuccess(userId: Int, token: String, after: () -> Unit = {}) {
        memberAPI.getMemberDetail("Bearer $token", userId).callOnSuccess {
            setLoginInfo(
                LoginInfo(
                    id = userId,
                    token = token,
                    name = it.name,
                    tel = it.tel,
                    email = it.email,
                    birth = it.birth,
                    profileImageUrl = it.profileImageUrl,
                    gender = Gender.MAN.id,
                    status = UserStatus.MEMBER.id,
                    joinType = it.joinType,
                    doAdultCert = it.doAdultCert,
                    doMarketingAgree = false,
                    doNoticeSend = false,
                    likeCount = it.likeCount,
                    reviewCount = it.reviewCount,
                    couponCount = it.couponCount,
                    nickname = it.nickname
                )
            )
            _loginFlagLiveData.value = true
            after()
        }
    }

    /**
     * 로그아웃 시 실행되어야할 로직
     * 회원탈퇴 때도 포함된다.
     */
    fun logout() {
        loginInfoLiveData.value = null
        _loginFlagLiveData.value = false
//        SPUtils.getInstance(APP_SETTINGS_KEY).remove("initAddress")
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}