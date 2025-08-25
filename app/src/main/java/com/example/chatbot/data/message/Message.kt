package com.example.chatbot.data.message

data class Message(
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L
)
