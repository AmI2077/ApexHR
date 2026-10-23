package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.domain.NetworkAuthClient
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.auth.domain.AuthRepository

class AuthRepositoryImpl(
    private val networkAuthClient: NetworkAuthClient,
    private val tokenStorage: TokenStorage,
): AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> {
        return runCatching {
            val response = networkAuthClient.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            tokenStorage.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
        }
    }

    override suspend fun logout() {
        tokenStorage.clearTokens()
    }

    override suspend fun isAuthorized(): Boolean {
        // Эндпоинта обновления токена нет, поэтому достаточно access-токена.
        return !tokenStorage.getAccessToken().isNullOrBlank()
    }
}
