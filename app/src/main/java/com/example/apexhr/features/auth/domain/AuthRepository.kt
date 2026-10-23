package com.example.apexhr.features.auth.domain

interface AuthRepository {

    suspend fun login(
        email: String,
        password: String,
    ): LoginResult

    suspend fun logout()

    /** Проверяет сохранённую сессию локально и через GET /api/Auth/me. */
    suspend fun checkSession(): SessionStatus
}
