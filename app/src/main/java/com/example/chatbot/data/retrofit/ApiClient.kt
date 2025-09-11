package com.example.chatbot.data.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-inference.huggingface.co/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: HuggingFaceApi = retrofit.create(HuggingFaceApi::class.java)
}
