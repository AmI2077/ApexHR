package com.example.apexhr.features.auth.domain

sealed interface LoginResult {
    data object Success : LoginResult

    /** Вход выполнен, но сервер требует сменить временный пароль. */
    data object PasswordChangeRequired : LoginResult
    data class Failure(val error: AuthError) : LoginResult
}
