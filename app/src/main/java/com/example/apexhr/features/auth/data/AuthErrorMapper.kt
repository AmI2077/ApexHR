package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.features.auth.domain.AuthError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Переводит исключение сетевого слоя в доменную ошибку авторизации.
 * Проходит по цепочке cause, потому что Ktor и OkHttp часто оборачивают
 * исходную причину.
 */
fun Throwable.toAuthError(): AuthError {
    var current: Throwable? = this
    var depth = 0

    while (current != null && depth < MAX_CAUSE_DEPTH) {
        mapSingle(current)?.let { return it }
        current = current.cause?.takeIf { it !== current }
        depth++
    }

    return AuthError.Unknown
}

private fun mapSingle(throwable: Throwable): AuthError? = when (throwable) {
    is ApiStatusException -> when (throwable.statusCode) {
        400, 401 -> AuthError.InvalidCredentials
        408 -> AuthError.Timeout
        in 500..599 -> AuthError.Server
        else -> AuthError.Unknown
    }

    // Таймауты проверяем раньше IOException: это её подклассы.
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> AuthError.Timeout

    is IOException -> AuthError.Network

    else -> null
}

private const val MAX_CAUSE_DEPTH = 10
