package com.example.chatbot.data

import com.example.chatbot.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.IOException

// LoginDataSource.kt

// Constructor'a FirebaseAuth parametresi ekleyin
class LoginDataSource(private val firebaseAuth: FirebaseAuth) {

    fun login(email: String, password: String, callback: (Result<LoggedInUser>) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val loggedInUser = LoggedInUser(user.uid, user.displayName ?: user.email ?: "Unknown")
                        callback(Result.Success(loggedInUser))
                    } else {
                        callback(Result.Error(IOException("Kullanıcı bulunamadı.")))
                    }
                } else {
                    callback(Result.Error(IOException(task.exception?.message ?: "Giriş başarısız oldu.")))
                }
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}

