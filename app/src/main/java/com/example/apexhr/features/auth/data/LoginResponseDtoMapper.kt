package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.dto.response.LoginResponseDto
import com.example.apexhr.core.domain.AuthTokens

/**
 * @param nowMillis текущее время, от которого считается срок жизни токена.
 */
fun LoginResponseDto.toAuthTokens(nowMillis: Long): AuthTokens {
    return AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken?.takeIf { it.isNotBlank() },
        expiresAtMillis = expiresIn
            ?.takeIf { it > 0 }
            ?.let { nowMillis + it * MILLIS_IN_SECOND },
        // Нет поля — считаем, что смена пароля не нужна.
        mustChangePassword = mustChangePassword == true,
    )
}

private const val MILLIS_IN_SECOND = 1_000L