package com.example.truyvetyte

data class ChatMessage(
    val text: String,
    val isBot: Boolean // true nếu là Bot, false nếu là User
)