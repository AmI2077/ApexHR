package com.example.apexhr.core.data.network

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Переводит исключение сетевого слоя в доменную ошибку загрузки данных.
 * Проходит по цепочке cause, потому что Ktor и OkHttp часто оборачивают
 * исходную причину.
 */
fun Throwable.toDataError(): DataError {
    var current: Throwable? = this
    var depth = 0

    while (current != null && depth < MAX_CAUSE_DEPTH) {
        mapSingle(current)?.let { return it }
        current = current.cause?.takeIf { it !== current }
        depth++
    }

    return DataError.Unknown
}

/**
 * Выполняет запрос и заворачивает результат в [DataResult].
 * Отмену корутины не перехватывает, чтобы не ломать structured concurrency.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> {
    return try {
        DataResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        DataResult.Failure(e.toDataError())
    }
}

private fun mapSingle(throwable: Throwable): DataError? = when (throwable) {
    is ApiStatusException -> when (throwable.statusCode) {
        401 -> DataError.Unauthorized
        408 -> DataError.Timeout
        in 500..599 -> DataError.Server
        else -> DataError.Unknown
    }

    // Таймауты проверяем раньше IOException: это её подклассы.
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> DataError.Timeout

    is IOException -> DataError.Network

    else -> null
}

private const val MAX_CAUSE_DEPTH = 10
