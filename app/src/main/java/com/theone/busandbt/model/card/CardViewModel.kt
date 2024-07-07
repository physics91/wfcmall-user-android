package com.theone.busandbt.model.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.busandbt.code.CardType

class CardViewModel : ViewModel() {
    enum class CardRegistrationPage(val value: Int) {
        NUMBER_REGISTRATION(0),
        DATE_REGISTRATION(1),
        PASSWORD_REGISTRATION(2),
    }

    private val _currentPage = MutableLiveData<CardRegistrationPage>()
    val currentPage: LiveData<CardRegistrationPage> = _currentPage
    val memberId = MutableLiveData<Int?>()
    val cardNo = MutableLiveData<String?>()
    val expireYearMonth = MutableLiveData<String?>()
    val password = MutableLiveData<String?>()
    val birth = MutableLiveData<String?>()
    val simplePassword = MutableLiveData<String?>()
    val isCorpCard = MutableLiveData<Boolean?>()
    val selectedCardTypeLiveData = MutableLiveData<CardType?>()

    fun selectCardType(cardType: CardType?) {
        selectedCardTypeLiveData.value = cardType
    }


    fun setMemberId(id: Int) {
        memberId.value = id
    }

    fun setCardNo(cardNum: String) {
        cardNo.value = cardNum
    }

    fun setExpire(expireDate: String) {
        expireYearMonth.value = expireDate
    }

    fun setPassword(pw: String) {
        password.value = pw
    }

    fun setBirth(birthday: String) {
        birth.value = birthday
    }

    fun setSimplePassword(simplePw: String) {
        simplePassword.value = simplePw
    }

    fun setIsCorpCard(corpCard: Boolean) {
        isCorpCard.value = corpCard
    }


    fun updateCurrentPage(page: CardRegistrationPage) {
        _currentPage.value = page
    }

    fun resetAllFields() {
        memberId.value = null
        cardNo.value = null
        expireYearMonth.value = null
        password.value = null
        birth.value = null
        simplePassword.value = null
        _currentPage.value = CardRegistrationPage.NUMBER_REGISTRATION
    }
}