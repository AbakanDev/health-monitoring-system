package com.example.truyvetyte.model

data class CuaKhauListResponse(
    val success: Boolean,
    val message: String,
    val data: List<CuaKhauItem>?
)