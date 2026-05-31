package com.example.truyvetyte.model

data class DashboardSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: DashboardSummaryData?
)

data class DashboardSummaryData(
    val SoCaF0: Int,
    val SoCaF1F2: Int,
    val SoNguoiCachLy: Int,
    val SoVungNguyHiem: Int
)