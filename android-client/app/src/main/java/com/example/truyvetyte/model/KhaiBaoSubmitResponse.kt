package com.example.truyvetyte.model

data class KhaiBaoSubmitResponse(
    val success: Boolean,
    val message: String,
    val data: KhaiBaoSubmitData?
)

data class KhaiBaoSubmitData(
    val MaToKhai: String
)