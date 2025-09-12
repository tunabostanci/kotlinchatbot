package com.example.chatbot.data

import com.example.chatbot.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.IOException

class LoginDataSource {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return try {

            val authResult = auth.signInWithEmailAndPassword(username, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User not found")


            val snapshot = firestore.collection("users").document(userId).get().await()

            val displayName = snapshot.getString("name") ?: "Unknown User"

            val loggedInUser = LoggedInUser(userId, displayName)

            Result.Success(loggedInUser)
        } catch (e: Exception) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        auth.signOut()
    }
}
