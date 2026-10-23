package com.example.apexhr.features.auth.domain

sealed interface LoginResult {
    data object Success : LoginResult
    data class Failure(val error: AuthError) : LoginResult
}
