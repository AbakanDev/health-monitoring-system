package com.example.truyvetyte.model

data class KhaiBaoRequest(
    val TiepXucF0: Int,           // 0 hoặc 1
    val CoBenhNen: Int,           // 0 hoặc 1
    val ChiTietBenhNen: String?,  // null nếu không có bệnh nền
    val DiVeTuVungDich: Int,      // 0 hoặc 1
    val danhSachTrieuChung: List<String> // ["TC001", "TC002", ...]
)