package com.theone.busandbt.dto

/**
 * 선택 데이터를 정의한다.
 * [SelectionDialog]에서만 사용한다.
 * @param title 선택 항목에서 표시될 문구
 * @param data 항목을 선택했을때 반환될 데이터
 * @param isSelected 기본적으로 선택된 항목인지 판단하는 데이터
 */
data class Selection<T>(
    val title: String,
    val data: T,
    var isSelected: Boolean = false
)