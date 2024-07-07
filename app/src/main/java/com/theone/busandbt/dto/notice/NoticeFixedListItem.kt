package com.theone.busandbt.dto.notice

/**
 * 상단고정 간략 공지 데이터
 * 메인 화면에서 2초마다 교체되면서 보여줄 때 사용
 */
data class NoticeFixedListItem(
    val id: Int,
    val title: String
)
