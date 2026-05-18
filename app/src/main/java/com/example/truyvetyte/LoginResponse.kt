package com.example.truyvetyte

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val message: String,
    val token: String?,
    val data: UserData?
)

data class UserData(
    val MaNguoiDung: Int,
    val HoTen: String,
    val MaVaiTro: String
)