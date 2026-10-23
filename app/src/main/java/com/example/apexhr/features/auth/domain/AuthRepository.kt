package com.example.apexhr.features.auth.domain

interface AuthRepository {

    suspend fun login(
        login: String,
        password: String,
    ): Result<Unit>

    suspend fun logout()

    suspend fun isAuthorized(): Boolean
}