package com.example.apexhr.features.auth.domain

interface AuthRepository {

    suspend fun login(
        email: String,
        password: String,
    ): LoginResult

    suspend fun logout()

    suspend fun isAuthorized(): Boolean
}
