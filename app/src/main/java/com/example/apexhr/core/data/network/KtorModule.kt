package com.example.apexhr.core.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorModule {
    private const val LOG_TAG = "ApexHttp"

    fun provideAuthClient(json: Json, isDebug: Boolean): HttpClient {
        return HttpClient(OkHttp) {
            installCommon(json, isDebug)
        }
    }

    fun provideAuthorizedClient(json: Json, isDebug: Boolean): HttpClient {
        return HttpClient(OkHttp) {
            installCommon(json, isDebug)

            install(Auth) {
                bearer {

                }
            }
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
