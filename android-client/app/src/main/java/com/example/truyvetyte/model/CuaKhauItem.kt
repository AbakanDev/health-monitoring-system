package com.example.truyvetyte.model

data class CuaKhauItem(
    val maCuaKhau: String,
    val tenCuaKhau: String,
    val loaiCuaKhau: String,   // "Đường hàng không" | "Đường bộ" | "Đường biển"
    val trangThai: String
)