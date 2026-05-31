package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

data class TiemChungResponse(
    @SerializedName("message") val message: String,
    @SerializedName("soMuiTiem") val soMuiTiem: Int,
    @SerializedName("loaiThe") val loaiThe: String, // "XANH", "VANG", "DO"
    @SerializedName("danhSachTiem") val danhSachTiem: List<ChiTietTiem>?
)

data class ChiTietTiem(
    @SerializedName("muiSo") val muiSo: Int,
    @SerializedName("tenVaccine") val tenVaccine: String,
    @SerializedName("diaDiem") val diaDiem: String,
    @SerializedName("ngayTiem") val ngayTiem: String
)