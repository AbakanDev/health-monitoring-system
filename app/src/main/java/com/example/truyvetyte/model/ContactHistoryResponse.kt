package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

data class ContactHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("totalContacts") val totalContacts: Int?,
    @SerializedName("data") val data: List<ContactHistoryItem>?
)

data class ContactHistoryItem(
    @SerializedName("MaLichSuTiepXuc") val maLichSuTiepXuc: Int?,
    @SerializedName("ThoiGianTiepXuc") val thoiGianTiepXuc: String?,
    @SerializedName("DiaDiemTiepXuc") val diaDiemTiepXuc: String?,
    @SerializedName("TenNguoiTiepXuc") val tenNguoiTiepXuc: String?,
    @SerializedName("CapDoDichTeHienTai") val capDoDichTeHienTai: String?
)