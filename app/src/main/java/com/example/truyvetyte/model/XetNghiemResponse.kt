package com.example.truyvetyte.model

data class XetNghiemResponse(
    val success: Boolean,
    val message: String,
    val totalTests: Int,
    val data: List<XetNghiem>
)