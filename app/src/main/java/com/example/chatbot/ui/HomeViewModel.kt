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
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val db = FirebaseFirestore.getInstance()
    private val apiKey = BuildConfig.API_KEY

    init {

        db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _messages.value = snapshot.toObjects(Message::class.java)
                }
            }
    }

    fun sendMessage(text: String, senderId: String) {
        val message = Message(
            text = text,
            senderId = senderId,
            timestamp = System.currentTimeMillis()
        )

        // Kullanıcı mesajını Firestore'a ekle
        db.collection("messages").add(message)
        Log.d("chatbotLog", "Kullanıcı Mesajı: $text")

        // Eğer bot değilse ChatGPT'den cevap üret
        if (senderId != "bot") {
            generateBotReply(text)
        }
    }

    @OptIn(BetaOpenAI::class)
    private fun generateBotReply(userText: String) {
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
                    timestamp = System.currentTimeMillis()
                )
                db.collection("messages").add(botMessage)

            } catch (e: RateLimitException) {
                val botMessage = Message(
                    text = "Üzgünüm, API kotası doldu. Lütfen daha sonra tekrar deneyin.",
                    senderId = "bot",
                    timestamp = System.currentTimeMillis()
                )
                Log.d("chatbotLog", "Bot Mesajı: Üzgünüm, API kotası doldu. Lütfen daha sonra tekrar deneyin.")
                db.collection("messages").add(botMessage)
            } catch (e: Exception) {
                Log.e("chatbotLog", "Bot cevabı alınamadı", e)
            }
        }
    }

}
