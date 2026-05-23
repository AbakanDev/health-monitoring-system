package com.example.truyvetyte.model

data class ImmigrationSubmitResponse(
    val success: Boolean,
    val message: String,
    val data: ToKhaiData?
)

data class ToKhaiData(
    val MaToKhaiXNC: String
)