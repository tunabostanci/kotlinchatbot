package com.example.chatbot.data

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)
