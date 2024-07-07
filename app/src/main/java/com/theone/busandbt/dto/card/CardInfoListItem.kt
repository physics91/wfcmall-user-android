package com.theone.busandbt.dto.card

import com.busandbt.code.CardType

/**
 * 회원 카드 정보 데이터
 * @param id 카드 데이터 고유번호
 * @param type 카드사 유형 = [CardType]
 * @param cardNo 카드번호
 */
data class CardInfoListItem(
    val id: Int,
    val type: Int,
    val cardNo: String,
)