package com.example.apexhr.features.auth.ui.model

import com.example.apexhr.features.auth.domain.AuthError

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null,
)

sealed interface LoginUiActions {
    data object Success: LoginUiActions
}
