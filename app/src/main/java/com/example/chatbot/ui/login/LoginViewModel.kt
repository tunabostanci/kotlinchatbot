package com.example.chatbot.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.chatbot.data.LoginRepository
import com.example.chatbot.data.Result
import com.example.chatbot.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    // --- DÜZELTİLMİŞ KISIM ---
    fun login(username: String, password: String) {
        // loginRepository.login metoduna, işlem bittiğinde ne yapılacağını söyleyen
        // bir callback (lambda fonksiyonu) veriyoruz.
        loginRepository.login(username, password) { result ->
            // Bu kod bloğu, Firebase'den cevap geldiğinde çalışacak.
            if (result is Result.Success) {
                _loginResult.postValue( // Arka plandan UI'ı güvenle güncellemek için postValue kullanmak daha iyidir.
                    LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
                )
            } else {
                _loginResult.postValue(
                    LoginResult(error = R.string.login_failed)
                )
            }
        }
    }


    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
