package com.example.apexhr.core.data.network

import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/** Бросает [ApiStatusException], если сервер ответил не 2xx. */
fun HttpResponse.requireSuccess(): HttpResponse {
    if (!status.isSuccess()) {
        throw ApiStatusException(status.value)
    }
    return this
}
