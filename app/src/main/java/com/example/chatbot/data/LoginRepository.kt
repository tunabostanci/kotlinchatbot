package com.example.chatbot.data

import com.example.chatbot.data.model.LoggedInUser

class LoginRepository(private val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun login(email: String, password: String, callback: (Result<LoggedInUser>) -> Unit) {
        dataSource.login(email, password, callback)
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
    }
}