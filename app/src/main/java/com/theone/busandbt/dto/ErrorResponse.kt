package com.theone.busandbt.dto

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String
)