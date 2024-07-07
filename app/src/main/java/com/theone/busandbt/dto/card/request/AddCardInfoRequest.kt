package com.theone.busandbt.dto.card.request

import com.busandbt.code.CardType

/**
 * 회원 카드 등록 요청 데이터
 * @param memberId 대상 회원 고유번호
 * @param cardNo 카드사 유형 = [CardType]
 * @param expireYearMonth 카드유효기간 (YY/MM 형식으로 입력할 것)
 * @param password 카드 비밀번호
 * @param birth 생년월일
 * @param simplePassword 간편 비밀번호
 * @param isCorpCard  true= 개인타입 ,false = 법인카드
 */
data class AddCardInfoRequest(
    val memberId: Int,
    val cardNo: String,
    val expireYearMonth: String,
    val password: String?=null,
    val birth: String?=null,
    val simplePassword: String,
    val isCorpCard: Boolean
)