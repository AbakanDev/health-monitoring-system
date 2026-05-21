package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

data class TrendResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Int> // Mảng chứa các số nguyên tương ứng số ca F0 của 12 tháng
)