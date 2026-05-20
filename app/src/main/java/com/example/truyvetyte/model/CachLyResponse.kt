package com.example.truyvetyte.model

data class CachLyResponse(
    val success: Boolean,
    val message: String,
    val isQuarantined: Boolean,
    val data: List<CachLy>
)