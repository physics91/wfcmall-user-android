package com.theone.busandbt.type

/**
 * 카카오 주소검색 API 법정동/행정동 유형
 */
enum class RegionType(val code: String) {
    LEGAL("B"), // 법정동
    ADMIN("H"), // 행정동
    ;

    companion object {
        val VALUES = values().associateBy { it.code }
        fun find(code: String) = VALUES[code]
    }
}