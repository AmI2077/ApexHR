package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.domain.NetworkAuthClient
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.domain.LoginResult
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl(
    private val networkAuthClient: NetworkAuthClient,
    private val tokenStorage: TokenStorage,
): AuthRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        return try {
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

            LoginResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoginResult.Failure(e.toAuthError())
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
