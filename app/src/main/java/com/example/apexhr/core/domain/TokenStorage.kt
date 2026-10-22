package com.example.apexhr.core.domain

interface TokenStorage {

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    )

    suspend fun getAccessToken(): String

    suspend fun getRefreshToken(): String

    suspend fun clearTokens()
}