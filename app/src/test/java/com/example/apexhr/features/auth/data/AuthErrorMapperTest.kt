package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.features.auth.domain.AuthError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthErrorMapperTest {

    @Test
    fun `401 maps to invalid credentials`() {
        assertEquals(AuthError.InvalidCredentials, ApiStatusException(401).toAuthError())
    }

    @Test
    fun `400 maps to invalid credentials`() {
        assertEquals(AuthError.InvalidCredentials, ApiStatusException(400).toAuthError())
    }

    @Test
    fun `5xx maps to server error`() {
        assertEquals(AuthError.Server, ApiStatusException(500).toAuthError())
        assertEquals(AuthError.Server, ApiStatusException(503).toAuthError())
    }

    @Test
    fun `other status maps to unknown`() {
        assertEquals(AuthError.Unknown, ApiStatusException(404).toAuthError())
    }

    @Test
    fun `request timeout maps to timeout`() {
        val error = HttpRequestTimeoutException("https://example.com", 60_000L, null)
        assertEquals(AuthError.Timeout, error.toAuthError())
    }

    @Test
    fun `connect timeout maps to timeout`() {
        assertEquals(AuthError.Timeout, ConnectTimeoutException("timeout", null).toAuthError())
    }

    @Test
    fun `socket timeout maps to timeout`() {
        assertEquals(AuthError.Timeout, SocketTimeoutException("timeout").toAuthError())
    }

    @Test
    fun `no internet maps to network`() {
        assertEquals(AuthError.Network, UnknownHostException("host").toAuthError())
        assertEquals(AuthError.Network, ConnectException("refused").toAuthError())
        assertEquals(AuthError.Network, IOException("io").toAuthError())
    }

    @Test
    fun `wrapped cause is unwrapped`() {
        val wrapped = IllegalStateException("wrapper", SocketTimeoutException("timeout"))
        assertEquals(AuthError.Timeout, wrapped.toAuthError())
    }

    @Test
    fun `serialization error maps to unknown`() {
        assertEquals(AuthError.Unknown, SerializationException("bad json").toAuthError())
    }

    @Test
    fun `unexpected exception maps to unknown`() {
        assertEquals(AuthError.Unknown, IllegalStateException("boom").toAuthError())
    }
}
