package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.dto.request.ChangePasswordRequest
import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.NetworkAuthClient
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.domain.ChangePasswordResult
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

            val tokens = response.toAuthTokens(nowMillis = clock())
            tokenStorage.saveTokens(tokens)
            networkAuthClient.clearCachedTokens()

            if (tokens.mustChangePassword) {
                LoginResult.PasswordChangeRequired
            } else {
                LoginResult.Success
            }
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

    override suspend fun changePassword(newPassword: String): ChangePasswordResult {
        return try {
            networkAuthClient.changePassword(ChangePasswordRequest(newPassword = newPassword))

            // Токен после смены пароля остаётся действительным (так же
            // работает веб-версия), поэтому перелогин не нужен: только
            // снимаем флаг. Если токены уже удалены (401), сохранять нечего.
            tokenStorage.getTokens()?.let { tokens ->
                if (tokens.mustChangePassword) {
                    tokenStorage.saveTokens(tokens.copy(mustChangePassword = false))
                }
            }

            ChangePasswordResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ChangePasswordResult.Failure(e.toChangePasswordError())
        }
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

            // Флаг приходит только в ответе входа, поэтому берём сохранённый.
            if (tokens.mustChangePassword) {
                SessionStatus.PasswordChangeRequired
            } else {
                SessionStatus.Active
            }
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
