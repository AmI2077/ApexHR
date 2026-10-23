package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.data.dto.response.LoginResponseDto
import com.example.apexhr.core.data.dto.response.MeResponseDto
import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.AuthTokens
import com.example.apexhr.core.domain.NetworkAuthClient
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.auth.domain.AuthError
import com.example.apexhr.features.auth.domain.LoginResult
import com.example.apexhr.features.auth.domain.SessionStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.UnknownHostException

class AuthRepositoryImplTest {

    private val now = 1_000_000L
    private val storage = FakeTokenStorage()
    private val client = FakeNetworkAuthClient()
    private val repository = AuthRepositoryImpl(client, storage) { now }

    @Test
    fun `successful login saves tokens with expiration`() = runBlocking {
        client.loginResponse = LoginResponseDto(
            accessToken = "access",
            tokenType = "bearer",
            expiresIn = 3600,
            refreshToken = "refresh"
        )

        val result = repository.login("user@example.com", "secret")

        assertEquals(LoginResult.Success, result)
        assertEquals(LoginRequest("user@example.com", "secret"), client.lastLoginRequest)
        assertEquals(AuthTokens("access", "refresh", now + 3_600_000L), storage.tokens)
        assertEquals(1, client.clearCachedCalls)
    }

    @Test
    fun `login with wrong credentials returns invalid credentials`() = runBlocking {
        client.loginError = ApiStatusException(401)

        val result = repository.login("user@example.com", "wrong")

        assertEquals(LoginResult.Failure(AuthError.InvalidCredentials), result)
        assertNull(storage.tokens)
    }

    @Test
    fun `login without network returns network error`() = runBlocking {
        client.loginError = UnknownHostException("no network")

        val result = repository.login("user@example.com", "secret")

        assertEquals(LoginResult.Failure(AuthError.Network), result)
    }

    @Test
    fun `no tokens means not authorized`() = runBlocking {
        assertEquals(SessionStatus.NotAuthorized, repository.checkSession())
        assertEquals(0, client.meCalls)
    }

    @Test
    fun `valid token confirmed by server means active session`() = runBlocking {
        storage.tokens = AuthTokens("access", null, now + 3_600_000L)

        assertEquals(SessionStatus.Active, repository.checkSession())
        assertEquals(1, client.meCalls)
        assertNotNull(storage.tokens)
    }

    @Test
    fun `locally expired token clears session without network call`() = runBlocking {
        storage.tokens = AuthTokens("access", null, now - 1)

        assertEquals(SessionStatus.Expired, repository.checkSession())
        assertEquals(0, client.meCalls)
        assertNull(storage.tokens)
    }

    @Test
    fun `server 401 clears session`() = runBlocking {
        storage.tokens = AuthTokens("access", null, null)
        client.meError = ApiStatusException(401)

        assertEquals(SessionStatus.Expired, repository.checkSession())
        assertNull(storage.tokens)
    }

    @Test
    fun `network error keeps session and reports failure`() = runBlocking {
        val tokens = AuthTokens("access", null, now + 3_600_000L)
        storage.tokens = tokens
        client.meError = UnknownHostException("no network")

        assertEquals(SessionStatus.CheckFailed(AuthError.Network), repository.checkSession())
        assertEquals(tokens, storage.tokens)
    }

    @Test
    fun `server error keeps session and reports failure`() = runBlocking {
        storage.tokens = AuthTokens("access", null, null)
        client.meError = ApiStatusException(503)

        assertEquals(SessionStatus.CheckFailed(AuthError.Server), repository.checkSession())
        assertNotNull(storage.tokens)
    }

    @Test
    fun `logout clears tokens`() = runBlocking {
        storage.tokens = AuthTokens("access", "refresh", null)

        repository.logout()

        assertNull(storage.tokens)
        assertEquals(1, client.clearCachedCalls)
    }

    private class FakeTokenStorage : TokenStorage {
        var tokens: AuthTokens? = null

        override suspend fun saveTokens(tokens: AuthTokens) {
            this.tokens = tokens
        }

        override suspend fun getTokens(): AuthTokens? = tokens

        override suspend fun clearTokens() {
            tokens = null
        }
    }

    private class FakeNetworkAuthClient : NetworkAuthClient {
        var loginResponse = LoginResponseDto(accessToken = "access")
        var loginError: Exception? = null
        var meError: Exception? = null
        var lastLoginRequest: LoginRequest? = null
        var meCalls = 0
        var clearCachedCalls = 0

        override suspend fun login(loginRequest: LoginRequest): LoginResponseDto {
            lastLoginRequest = loginRequest
            loginError?.let { throw it }
            return loginResponse
        }

        override suspend fun getCurrentUser(): MeResponseDto {
            meCalls++
            meError?.let { throw it }
            return MeResponseDto(message = "ok", userId = "id")
        }

        override fun clearCachedTokens() {
            clearCachedCalls++
        }
    }
}
