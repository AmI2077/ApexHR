package com.example.apexhr.features.auth.ui.model

import com.example.apexhr.features.auth.domain.AuthError
import com.example.apexhr.features.auth.domain.EmailError
import com.example.apexhr.features.auth.domain.PasswordError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    /** Ошибка ответа сервера или сети. */
    val error: AuthError? = null,
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
)

sealed interface LoginUiActions {
    data object Success: LoginUiActions
    data object PasswordChangeRequired: LoginUiActions
}
