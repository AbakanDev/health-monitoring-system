package com.example.truyvetyte.model

import com.google.gson.annotations.SerializedName

data class VaccineRatesResponse(
    val success: Boolean,
    val message: String,
    val data: VaccineRateData?
)

data class VaccineRateData(
    @SerializedName("TyLeMui1") val tyLeMui1: String,
    @SerializedName("TyLeMui2") val tyLeMui2: String
)