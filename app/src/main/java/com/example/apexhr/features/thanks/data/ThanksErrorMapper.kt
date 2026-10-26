package com.example.apexhr.features.thanks.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.toDataError
import com.example.apexhr.features.thanks.domain.ThanksSendError
import kotlinx.coroutines.CancellationException

/** Отказ сервера -> доменная ошибка отправки. */
fun ThanksApiException.toSendError(): ThanksSendError = when {
    statusCode == 401 || statusCode == 408 || statusCode >= 500 ->
        ThanksSendError.Data(ApiStatusException(statusCode).toDataError())

    statusCode == 403 -> ThanksSendError.Forbidden
    statusCode == 404 -> ThanksSendError.NotFound
    else -> ThanksSendError.Rejected(serverMessage)
}

/** Любое исключение отправки -> доменная ошибка. */
fun Throwable.toThanksSendError(): ThanksSendError =
    if (this is ThanksApiException) toSendError() else ThanksSendError.Data(toDataError())

/** Выполняет действие; отмену корутины не перехватывает. */
internal suspend fun <T> safeThanksAction(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
