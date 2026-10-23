package com.example.apexhr.core.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ GET /api/Auth/me: { "message": "You are authenticated!", "userId": "..." }.
 * Используется только для проверки, что токен ещё действителен.
 */
@Serializable
data class MeResponseDto(
    @SerialName("message")
    val message: String? = null,

    @SerialName("userId")
    val userId: String? = null,
)
