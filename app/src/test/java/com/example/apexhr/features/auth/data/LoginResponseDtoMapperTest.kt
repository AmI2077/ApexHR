package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.dto.response.LoginResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginResponseDtoMapperTest {

    @Test
    fun `expiresIn is converted to absolute expiration time`() {
        val tokens = LoginResponseDto(
            accessToken = "access",
            tokenType = "bearer",
            expiresIn = 3600,
            refreshToken = "refresh"
        ).toAuthTokens(nowMillis = 1_000L)

        assertEquals("access", tokens.accessToken)
        assertEquals("refresh", tokens.refreshToken)
        assertEquals(1_000L + 3_600_000L, tokens.expiresAtMillis)
    }

    @Test
    fun `missing expiresIn and blank refresh token become null`() {
        val tokens = LoginResponseDto(
            accessToken = "access",
            expiresIn = null,
            refreshToken = " "
        ).toAuthTokens(nowMillis = 1_000L)

        assertNull(tokens.expiresAtMillis)
        assertNull(tokens.refreshToken)
    }

    @Test
    fun `non positive expiresIn is ignored`() {
        val tokens = LoginResponseDto(accessToken = "access", expiresIn = 0)
            .toAuthTokens(nowMillis = 1_000L)

        assertNull(tokens.expiresAtMillis)
    }
}
