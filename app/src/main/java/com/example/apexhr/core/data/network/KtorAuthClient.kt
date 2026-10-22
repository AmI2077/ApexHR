package com.example.apexhr.core.data.network

import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.data.dto.response.LoginResponseDto
import com.example.apexhr.core.data.network.NetworkConfig.BASE_LOGIN_URL
import com.example.apexhr.core.domain.NetworkAuthClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorAuthClient(
    private val client: HttpClient
): NetworkAuthClient {
    override suspend fun login(loginRequest: LoginRequest): LoginResponseDto {
        return client.post(BASE_LOGIN_URL) {
            contentType(ContentType.Application.Json)

            setBody(
                loginRequest
            )
        }.body<LoginResponseDto>()
    }
}