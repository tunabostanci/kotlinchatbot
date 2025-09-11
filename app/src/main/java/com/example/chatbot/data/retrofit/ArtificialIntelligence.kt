package com.example.chatbot.data.retrofit

import kotlinx.coroutines.*

class ArtificialIntelligence {
    fun sendMessageToBot(userMessage: String, apiKey: String, onResult: (String) -> Unit)  {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.api.generateText(
                    body = mapOf("inputs" to userMessage),
                    token = "Bearer $apiKey"
                )

                val botReply = ((response["generated_text"] ?: "") as String)

                withContext(Dispatchers.Main) {
                    onResult(botReply)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult("Bot ile bağlantı kurulamadı.")
                }
            }
        }

    }
}