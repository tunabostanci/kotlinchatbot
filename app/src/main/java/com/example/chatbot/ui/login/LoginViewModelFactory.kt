package com.example.chatbot.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatbot.data.LoginDataSource
import com.example.chatbot.data.LoginRepository
import com.google.firebase.auth.FirebaseAuth


// LoginViewModelFactory.kt

class LoginViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            // Bağımlılıkları burada, en tepede oluşturun ve aşağı doğru paslayın
            val firebaseAuth = FirebaseAuth.getInstance()
            val dataSource = LoginDataSource(firebaseAuth)
            val repository = LoginRepository(dataSource)

            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
