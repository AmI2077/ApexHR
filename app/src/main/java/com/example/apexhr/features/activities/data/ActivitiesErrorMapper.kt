package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.toDataError
import com.example.apexhr.features.activities.domain.ActivityActionError
import kotlinx.coroutines.CancellationException

/** Отказ сервера -> доменная ошибка действия. */
fun ActivitiesApiException.toActionError(): ActivityActionError = when {
    statusCode == 401 || statusCode == 408 || statusCode >= 500 ->
        ActivityActionError.Data(ApiStatusException(statusCode).toDataError())

    statusCode == 403 -> ActivityActionError.Forbidden
    statusCode == 404 -> ActivityActionError.NotFound
    else -> ActivityActionError.Rejected(serverMessage)
}

/** Любое исключение действия -> доменная ошибка. */
fun Throwable.toActivityActionError(): ActivityActionError =
    if (this is ActivitiesApiException) toActionError() else ActivityActionError.Data(toDataError())

/** Выполняет действие; отмену корутины не перехватывает. */
internal suspend fun <T> safeActivityAction(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
