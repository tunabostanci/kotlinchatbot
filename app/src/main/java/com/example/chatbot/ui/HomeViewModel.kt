package com.example.chatbot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatbot.data.message.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class HomeViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val db = FirebaseFirestore.getInstance()

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
    }
}
