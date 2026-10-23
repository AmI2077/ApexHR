package com.example.apexhr.core.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ POST /api/Auth/login.
 * В Swagger схема не описана, поля сверены с реальным ответом сервера:
 * { "accessToken": "...", "tokenType": "bearer", "expiresIn": 3600, "refreshToken": "..." }
 * Обязателен только accessToken, остальные поля опциональны.
 */
@Serializable
data class LoginResponseDto(
    @SerialName("accessToken")
    val accessToken: String,

    @SerialName("tokenType")
    val tokenType: String? = null,

    /** Время жизни access-токена в секундах. */
    @SerialName("expiresIn")
    val expiresIn: Long? = null,

    @SerialName("refreshToken")
    val refreshToken: String? = null,
)
