package com.example.apexhr.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorModule {
    fun provideAuthClient(json: Json): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    fun provideAuthorizedClient(json: Json): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }

            install(Auth) {
                bearer {

                }
            }
        }
    }
}