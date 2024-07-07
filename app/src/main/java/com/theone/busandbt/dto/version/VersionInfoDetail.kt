package com.theone.busandbt.dto.version

data class VersionInfoDetail(
    val code: Int? = null,
    val name: String,
    val updateType: Int,
    val updateUrl: String? = null,
    val desc: String? = null,
)