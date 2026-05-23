package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

// ── Dữ liệu khu vực trả về từ server ──────────────────────────────────────────
data class CheckInData(
    @SerializedName("TenKhuVuc")      val tenKhuVuc: String,
    @SerializedName("ThoiGianCheckIn") val thoiGianCheckIn: String,
    @SerializedName("TrangThaiKhuVuc") val trangThaiKhuVuc: String
)

// ── Wrapper toàn bộ response ───────────────────────────────────────────────────
data class CheckInResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data")    val data: CheckInData?
)