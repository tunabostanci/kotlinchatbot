package com.example.chatbot.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatbot.data.message.Message
import com.example.chatbot.data.retrofit.ApiClient
import com.example.chatbot.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val db = FirebaseFirestore.getInstance()

    private val huggingFaceApiKey = BuildConfig.API_KEY

    init {
        db.collection("messages").orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val list = snapshot.toObjects(Message::class.java)
                    _messages.value = list
                }
            }
    }

    fun sendMessage(text: String, senderId: String) {
        val message = Message(
            text = text,
            senderId = senderId,
            timestamp = System.currentTimeMillis()
        )
        db.collection("messages").add(message)
        Log.d("chatbotLog", "Kullanıcı Mesajı: $text")

        if (senderId != "bot") {
            generateBotReply(text)
        }
    }

    private fun generateBotReply(userText: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.api.generateText(
                    body = mapOf("inputs" to userText),
                    token = "Bearer $huggingFaceApiKey"
                )

                val botText = (response["generated_text"] ?: "Üzgünüm, cevap veremedim.") as String
                Log.d("chatbotLog", "Bot Yanıtı: $botText")
                val botMessage = Message(
                    text = botText,
                    senderId = "bot",
                    timestamp = System.currentTimeMillis()
                )

                db.collection("messages").add(botMessage)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
