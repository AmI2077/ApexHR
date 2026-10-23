package com.example.apexhr.core.domain

import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.data.dto.response.LoginResponseDto
import com.example.apexhr.core.data.dto.response.MeResponseDto

interface NetworkAuthClient {

    suspend fun login(loginRequest: LoginRequest): LoginResponseDto

    /** GET /api/Auth/me с текущим access-токеном. */
    suspend fun getCurrentUser(): MeResponseDto

    /** Сбрасывает закэшированный в Ktor bearer-токен (после входа и выхода). */
    fun clearCachedTokens()
}
