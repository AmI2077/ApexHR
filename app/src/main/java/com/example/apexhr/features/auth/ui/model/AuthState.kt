package com.example.apexhr.features.auth.ui.model

sealed interface AuthState {
    data object Checking : AuthState
    data object Authorized : AuthState
    data object Unauthorized : AuthState
    data class Error(val message: String) : AuthState
}