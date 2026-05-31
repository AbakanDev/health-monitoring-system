package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

data class ImmigrationHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<ImmigrationHistoryItem>
)

data class ImmigrationHistoryItem(
    @SerializedName("MaToKhaiXNC") val maToKhaiXNC: String?,
    @SerializedName("Ngay") val ngay: String?,
    @SerializedName("Gio") val gio: String?,
    @SerializedName("TenCuaKhau") val tenCuaKhau: String?,
    @SerializedName("LoaiCuaKhau") val loaiCuaKhau: String?,
    @SerializedName("TrangThaiCuaKhau") val trangThaiCuaKhau: String?,
    @SerializedName("HoTen") val hoTen: String?,
    @SerializedName("CCCD") val cccd: String?
)