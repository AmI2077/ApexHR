package com.example.apexhr.features.auth.ui.model

import com.example.apexhr.features.auth.domain.AuthError

sealed interface AuthState {
    data object Checking : AuthState
    data object Authorized : AuthState

    /** Вход выполнен, но до приложения нужно сменить временный пароль. */
    data object PasswordChangeRequired : AuthState

    /** @param isSessionExpired показать на экране входа, что сессия истекла. */
    data class Unauthorized(val isSessionExpired: Boolean = false) : AuthState

    data class Error(val error: AuthError) : AuthState
}
