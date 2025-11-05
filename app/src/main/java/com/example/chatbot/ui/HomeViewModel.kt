import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.data.message.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.exception.RateLimitException
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.chatbot.BuildConfig
import com.example.chatbot.data.message.Conversation
import kotlinx.coroutines.launch
import com.google.firebase.firestore.toObjects
import java.util.UUID

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val apiKey = BuildConfig.API_KEY
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _conversations = MutableLiveData<List<Conversation>>()
    val conversations: LiveData<List<Conversation>> = _conversations

    private val _selectedConversationId = MutableLiveData<String>()

    init {
        Log.d("chatbotLog", "API Key: $apiKey")
        loadConversations()

        _selectedConversationId.observeForever { conversationId ->
            _messages.value = emptyList()
            loadMessagesForConversation(conversationId)
        }
    }

    private fun loadConversations() {
        currentUserId?.let { userId ->
            db.collection("users").document(userId).collection("conversations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("chatbotLog", "Error loading conversations", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val conversationList = snapshot.toObjects<Conversation>()
                        _conversations.value = conversationList
                        if (conversationList.isNotEmpty() && _selectedConversationId.value == null) {
                            _selectedConversationId.value = conversationList.first().id
                        } else if (conversationList.isEmpty()) {
                            startNewConversation()
                        }
                    }
                }
        }
    }

    private fun loadMessagesForConversation(conversationId: String) {
        db.collection("messages")
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("chatbotLog", "Error loading messages", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _messages.value = snapshot.toObjects(Message::class.java)
                }
            }
    }

    fun selectConversation(conversationId: String) {
        _selectedConversationId.value = conversationId
    }

    fun startNewConversation() {
        currentUserId?.let { userId ->
            val newConversationId = UUID.randomUUID().toString()
            val newConversation = Conversation(id = newConversationId, name = "New Chat - ${System.currentTimeMillis()}", timestamp = System.currentTimeMillis())
            db.collection("users").document(userId).collection("conversations").document(newConversationId)
                .set(newConversation)
                .addOnSuccessListener {
                    _selectedConversationId.value = newConversationId
                }
                .addOnFailureListener { e ->
                    Log.e("chatbotLog", "Error starting new conversation", e)
                }
        }
    }

    fun sendMessage(text: String, senderId: String) {
        _selectedConversationId.value?.let { conversationId ->
            val message = Message(
                text = text,
                senderId = senderId,
                timestamp = System.currentTimeMillis(),
                conversationId = conversationId
            )

            val currentMessages = _messages.value ?: emptyList()
            _messages.value = currentMessages + message

            db.collection("messages").add(message)
                .addOnSuccessListener {
                    Log.d("chatbotLog", "Kullanıcı Mesajı: $text")
                    currentUserId?.let { userId ->
                        db.collection("users").document(userId).collection("conversations").document(conversationId)
                            .update("timestamp", System.currentTimeMillis())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("chatbotLog", "Error sending message", e)
                }


            if (senderId != "bot") {
                generateBotReply(text, conversationId)
            }
        }
    }

    @OptIn(BetaOpenAI::class)
    private fun generateBotReply(userText: String, conversationId: String) {
        viewModelScope.launch {
            try {
                val openAI = OpenAI(apiKey)
                val chatCompletion = openAI.chatCompletion(
                    request = ChatCompletionRequest(
                        model = ModelId("gpt-3.5-turbo"),
                        messages = listOf(
                            ChatMessage(role = ChatRole.User, content = userText)
                        ),
                        maxTokens = 150
                    )
                )

                val botText = chatCompletion.choices.first().message?.content
                    ?: "Üzgünüm, cevap veremedim."

                Log.d("chatbotLog", "Bot Yanıtı: $botText")

                val botMessage = Message(
                    text = botText,
                    senderId = "bot",
                    timestamp = System.currentTimeMillis(),
                    conversationId = conversationId
                )
                val currentMessages = _messages.value ?: emptyList()
                _messages.value = currentMessages + botMessage
                db.collection("messages").add(botMessage)

            } catch (e: RateLimitException) {
                val botMessage = Message(
                    text = "Üzgünüm, API kotası doldu. Lütfen daha sonra tekrar deneyin.",
                    senderId = "bot",
                    timestamp = System.currentTimeMillis(),
                    conversationId = conversationId
                )
                Log.d("chatbotLog", "Bot Mesajı: Üzgünüm, API kotası doldu. Lütfen daha sonra tekrar deneyin.")
                val currentMessages = _messages.value ?: emptyList()
                _messages.value = currentMessages + botMessage
                db.collection("messages").add(botMessage)
            } catch (e: Exception) {
                Log.e("chatbotLog", "Bot cevabı alınamadı", e)
            }
        }
    }
}