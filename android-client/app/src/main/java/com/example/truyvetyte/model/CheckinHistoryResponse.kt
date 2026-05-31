package com.example.truyvetyte.model

data class CheckinHistoryResponse(
    val success: Boolean,
    val message: String,
    val totalCheckins: Int?,
    val data: List<CheckinHistoryItem>?
)

data class CheckinHistoryItem(
    val TenKhuVuc: String?,
    val ThoiGianCheckIn: String?,
    val TrangThaiKhuVuc: String?
)