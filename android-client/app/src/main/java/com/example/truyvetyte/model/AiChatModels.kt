package com.example.truyvetyte.model
import com.google.gson.annotations.SerializedName

data class AiAskRequest(
    @SerializedName("question") val question: String
)

data class AiAskResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("answer") val answer: String?
)