package com.example.truyvetyte.model

data class DashboardOverviewResponse(
    val success: Boolean,
    val message: String,
    val data: DashboardOverviewData?
)

data class DashboardOverviewData(
    val TongCaBenh: Int,
    val TongBenhNhan: Int,
    val TiemChungThangNay: Int,
    val XetNghiemThangNay: Int,
    val CachLyThangNay: Int
)