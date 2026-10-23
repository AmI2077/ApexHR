package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.NetworkAuthClient
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.domain.LoginResult
import com.example.apexhr.features.auth.domain.SessionStatus
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl(
    private val networkAuthClient: NetworkAuthClient,
    private val tokenStorage: TokenStorage,
    private val clock: () -> Long = System::currentTimeMillis,
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
                response.toAuthTokens(nowMillis = clock())
            )
            networkAuthClient.clearCachedTokens()

            LoginResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoginResult.Failure(e.toAuthError())
        }
    }

    override suspend fun logout() {
        tokenStorage.clearTokens()
        networkAuthClient.clearCachedTokens()
    }

    override suspend fun checkSession(): SessionStatus {
        return try {
            val tokens = tokenStorage.getTokens()
                ?: return SessionStatus.NotAuthorized

            val expiresAt = tokens.expiresAtMillis
            if (expiresAt != null && clock() >= expiresAt - EXPIRATION_LEEWAY_MILLIS) {
                logout()
                return SessionStatus.Expired
            }

            networkAuthClient.getCurrentUser()
            SessionStatus.Active
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiStatusException) {
            if (e.statusCode == UNAUTHORIZED) {
                logout()
                SessionStatus.Expired
            } else {
                SessionStatus.CheckFailed(e.toAuthError())
            }
        } catch (e: Exception) {
            // Сеть или сервер недоступны: сессию не сбрасываем, даём повторить.
            SessionStatus.CheckFailed(e.toAuthError())
        }
    }

    private companion object {
        /** Считаем токен истёкшим чуть раньше, чтобы не словить 401 сразу после старта. */
        const val EXPIRATION_LEEWAY_MILLIS = 30_000L
        const val UNAUTHORIZED = 401
    }
}
