package com.theone.busandbt.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.busandbt.code.Gender
import com.busandbt.code.JoinType
import com.busandbt.code.TelecomType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

class JoinInfoViewModel : ViewModel() {
    val infoId = MutableLiveData<String>()
    val infoPassword = MutableLiveData<String>()
    val nameLiveData = MutableLiveData<String>()
    val nicknameLiveData = MutableLiveData<String>()
    val emailLiveData = MutableLiveData<String>()
    val genderLiveData = MutableLiveData<Gender>()
    val telecomTypeLiveData = MutableLiveData<TelecomType>()
    val birthLiveData = MutableLiveData<LocalDate>()
    private val joinSmsExpireSecondsLiveData = MutableLiveData<Int>()
    private var joinSmsExpireSeconds: Int = 0
    var memberId: Int? = null
    var tempToken: String? = null
    var certed: Boolean = false
    var sendJoinSms: Boolean = false
    var joinType = JoinType.NORMAL
    var snsClientId: String? = null
    var countDownJob: Job? = null

    fun setLoginId(id: String) {
        infoId.value = id
    }

    fun removeLoginId() {
        infoId.value = ""
    }

    fun setLoginPassword(pw: String) {
        infoPassword.value = pw
    }

    fun setName(name: String) {
        nameLiveData.value = name
    }

    fun setNickname(nickname: String) {
        nicknameLiveData.value = nickname
    }

    fun setEmail(email: String) {
        emailLiveData.value = email
    }

    fun setBirth(birth: LocalDate) {
        birthLiveData.value = birth
    }

    fun setGender(gender: Gender) {
        genderLiveData.value = gender
    }

    fun setTelecomType(telecomType: TelecomType) {
        telecomTypeLiveData.value = telecomType
    }

    fun setJoinSmsExpireSeconds(seconds: Int) {
        joinSmsExpireSeconds = seconds
        joinSmsExpireSecondsLiveData.value = seconds
        countDownJob?.cancel()
        countDownJob = CoroutineScope(Dispatchers.Default).launch {
            while (joinSmsExpireSeconds > 0) {
                delay(1000L)
                joinSmsExpireSeconds--
                launch(Dispatchers.Main) {
                    joinSmsExpireSecondsLiveData.value = joinSmsExpireSeconds
                }
            }
        }
    }

    fun getCurrentJoinSmsExpireSeconds(): Int = joinSmsExpireSeconds
}