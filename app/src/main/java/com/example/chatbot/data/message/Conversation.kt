package com.example.chatbot.data.message

data class Conversation(
    val id: String = "",
    val name: String = "New Chat",
    val timestamp: Long = System.currentTimeMillis()
)
