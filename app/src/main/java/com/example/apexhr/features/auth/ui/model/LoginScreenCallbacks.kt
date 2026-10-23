package com.example.apexhr.features.auth.ui.model

interface LoginScreenCallbacks {
    fun onEmailChange(value: String)
    fun onPasswordChange(value: String)
    fun onLoginClick()

    companion object {
        val Empty = object : LoginScreenCallbacks {
            override fun onEmailChange(value: String) = Unit
            override fun onPasswordChange(value: String) = Unit
            override fun onLoginClick() = Unit
        }
    }
}
