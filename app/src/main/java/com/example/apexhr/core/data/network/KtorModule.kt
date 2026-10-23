package com.example.apexhr.core.data.network

import android.util.Log
import com.example.apexhr.core.domain.SessionEventBus
import com.example.apexhr.core.domain.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.clearAuthTokens
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorModule {
    private const val LOG_TAG = "ApexHttp"

    fun provideAuthClient(json: Json, isDebug: Boolean): HttpClient {
        return HttpClient(OkHttp) {
            installCommon(json, isDebug)
        }
    }

    /**
     * Клиент для запросов от имени пользователя. Подставляет access-токен
     * из TokenStorage. На любой 401 очищает токены и сообщает в
     * SessionEventBus, что сессия истекла.
     */
    fun provideAuthorizedClient(
        json: Json,
        isDebug: Boolean,
        tokenStorage: TokenStorage,
        sessionEventBus: SessionEventBus,
    ): HttpClient {
        return HttpClient(OkHttp) {
            installCommon(json, isDebug)

            install(Auth) {
                bearer {
                    loadTokens {
                        tokenStorage.getAccessToken()?.let { accessToken ->
                            BearerTokens(accessToken = accessToken, refreshToken = null)
                        }
                    }

                    // В API нет эндпоинта обновления токена: при 401 сразу
                    // отдаём ответ дальше, его обработает UnauthorizedHandler.
                    refreshTokens { null }

                    // Токен отправляем сразу, без ожидания 401-челленджа,
                    // и только на наш бэкенд.
                    sendWithoutRequest { request ->
                        request.url.host == NetworkConfig.HOST
                    }
                }
            }

            install(
                createClientPlugin("UnauthorizedHandler") {
                    val httpClient = client

                    onResponse { response ->
                        if (response.status == HttpStatusCode.Unauthorized) {
                            tokenStorage.clearTokens()
                            httpClient.clearAuthTokens()
                            sessionEventBus.notifySessionExpired()
                        }
                    }
                }
            )
        }
    }

    private fun HttpClientConfig<OkHttpConfig>.installCommon(
        json: Json,
        isDebug: Boolean,
    ) {
        // Коды ответа проверяем вручную в клиентах, чтобы отличать 401 от 5xx.
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = NetworkConfig.REQUEST_TIMEOUT_MILLIS
            connectTimeoutMillis = NetworkConfig.CONNECT_TIMEOUT_MILLIS
            socketTimeoutMillis = NetworkConfig.SOCKET_TIMEOUT_MILLIS
        }

        if (isDebug) {
            install(Logging) {
                // INFO: только метод, URL и код ответа. Тела запросов и ответов
                // (пароль, токены) в лог не попадают.
                level = LogLevel.INFO
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(LOG_TAG, message)
                    }
                }
            }
        }
    }
}
