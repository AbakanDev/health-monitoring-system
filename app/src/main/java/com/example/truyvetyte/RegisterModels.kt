package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

// Cấu trúc dữ liệu App gửi lên Backend (Phải khớp 100% với tên biến trong req.body của Node.js)
data class RegisterRequest(
    val cccd: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    val dob: String,
    val gender: String,
    val address: String,
    val email: String,
    val phone: String
)

// Cấu trúc dữ liệu Backend trả về App
data class RegisterResponse(
    val status: String,
    val message: String
)