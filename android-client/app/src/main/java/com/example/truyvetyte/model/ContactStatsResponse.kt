package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

data class ContactStatsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ContactStatsData?
)

data class ContactStatsData(
    @SerializedName("SoLuotF0") val SoLuotF0: String,
    @SerializedName("SoLuotF1") val SoLuotF1: String,
    @SerializedName("SoLuotF2") val SoLuotF2: String,
    @SerializedName("TongLuotTiepXuc") val TongLuotTiepXuc: Int
)