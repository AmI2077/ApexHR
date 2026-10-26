package com.example.apexhr.core.data.network

import com.example.apexhr.core.data.dto.request.ChangePasswordRequest
import com.example.apexhr.core.data.dto.request.LoginRequest
import com.example.apexhr.core.data.dto.response.LoginResponseDto
import com.example.apexhr.core.data.dto.response.MeResponseDto
import com.example.apexhr.core.data.network.NetworkConfig.BASE_LOGIN_URL
import com.example.apexhr.core.data.network.NetworkConfig.CHANGE_PASSWORD_URL
import com.example.apexhr.core.data.network.NetworkConfig.CURRENT_USER_URL
import com.example.apexhr.core.domain.NetworkAuthClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.clearAuthTokens
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * @param client клиент без авторизации, для логина.
 * @param authorizedClient клиент с bearer-токеном, для запросов от имени пользователя.
 */
class KtorAuthClient(
    private val client: HttpClient,
    private val authorizedClient: HttpClient,
): NetworkAuthClient {
    override suspend fun login(loginRequest: LoginRequest): LoginResponseDto {
        val response = client.post(BASE_LOGIN_URL) {
            contentType(ContentType.Application.Json)

            setBody(
                loginRequest
            )
        }

        return response.requireSuccess().body<LoginResponseDto>()
    }

    override suspend fun getCurrentUser(): MeResponseDto {
        return authorizedClient.get(CURRENT_USER_URL)
            .requireSuccess()
            .body<MeResponseDto>()
    }

    override suspend fun changePassword(request: ChangePasswordRequest) {
        // Тело успешного ответа в Swagger не описано и не разбирается.
        authorizedClient.post(CHANGE_PASSWORD_URL) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireSuccessWithMessage()
    }

    override fun clearCachedTokens() {
        authorizedClient.clearAuthTokens()
    }

    private fun HttpResponse.requireSuccess(): HttpResponse {
        if (!status.isSuccess()) {
            throw ApiStatusException(status.value)
        }
        return this
    }
}
