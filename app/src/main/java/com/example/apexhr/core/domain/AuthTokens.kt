package com.example.apexhr.core.domain

/**
 * Токены текущей сессии.
 *
 * @param expiresAtMillis момент истечения access-токена (epoch millis),
 * null если сервер не прислал expiresIn.
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAtMillis: Long? = null,
)
