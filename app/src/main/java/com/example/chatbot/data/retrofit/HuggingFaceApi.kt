package com.example.chatbot.data.retrofit

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface HuggingFaceApi {
    @POST("models/gpt2")
    suspend fun generateText(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Map<String, Any>
}
