package com.example.truyvetyte.model

data class CheckinStatsResponse(
    val success: Boolean,
    val message: String,
    val data: CheckinStatsData?
)

data class CheckinStatsData(
    val TongLuotCheckIn: Int,
    val SoLuotNguyHiem: String,
    val SoLuotNguyCo: String,
    val SoLuotAnToan: String
)