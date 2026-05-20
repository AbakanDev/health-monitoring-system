package com.example.truyvetyte

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String?,
    @SerializedName("data") val data: UserData?
)

data class UserData(
    @SerializedName("MaNguoiDung") val MaNguoiDung: Int,
    @SerializedName("HoTen") val HoTen: String,
    @SerializedName("MaVaiTro") val MaVaiTro: String,
    @SerializedName("CCCD") val CCCD: String
)