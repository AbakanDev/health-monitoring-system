package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

// Class đại diện cho dữ liệu gửi LÊN server (Bạn đã có)
data class RegisterRequest(
    @SerializedName("CCCD") val cccd: String,
    @SerializedName("MatKhau") val password: String,
    @SerializedName("HoTen") val fullName: String,
    @SerializedName("NgaySinh") val dob: String,
    @SerializedName("GioiTinh") val gender: String,
    @SerializedName("DiaChi") val address: String,
    @SerializedName("Email") val email: String,
    @SerializedName("SDT") val phone: String
)

// THÊM CLASS NÀY: Đại diện cho dữ liệu server Node.js TRẢ VỀ sau khi đăng ký
data class RegisterModels(
    @SerializedName("message") val message: String, // Ví dụ backend trả về {"message": "Đăng ký thành công!"}
    @SerializedName("status") val status: Int? = null
    // Thêm các trường khác tùy thuộc vào việc API /api/auth/register của bạn trả về những gì
)