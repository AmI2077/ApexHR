package com.example.apexhr.features.auth.ui.model

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
)

sealed interface LoginUiActions {
    data object Success: LoginUiActions
}
