package com.theone.busandbt.dto.notice

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 공지 리스트 데이터
 * @param id 공지 고유번호
 * @param title 공지 제목
 * @param fixed 상단공지 여부, true면 상단공지
 * @param
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NoticeListItem(
    val id: Int,
    val title: String,
    val fixed: Boolean,
    val createDateTime: String
)