package com.example.apexhr.core.domain

import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.data.dto.response.LoginResponseDto

interface NetworkAuthClient {

    suspend fun login(loginRequest: LoginRequest): LoginResponseDto
}