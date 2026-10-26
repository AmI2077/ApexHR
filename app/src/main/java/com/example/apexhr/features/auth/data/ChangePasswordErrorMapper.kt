package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.network.ApiMessageException
import com.example.apexhr.features.auth.domain.ChangePasswordError

/** Исключение при смене пароля -> доменная ошибка. */
fun Throwable.toChangePasswordError(): ChangePasswordError {
    val apiError = this as? ApiMessageException
        ?: return ChangePasswordError.Auth(toAuthError())

    return when (apiError.statusCode) {
        UNAUTHORIZED -> ChangePasswordError.SessionExpired
        REQUEST_TIMEOUT -> ChangePasswordError.Auth(toAuthError())
        in 400..499 -> ChangePasswordError.Rejected(apiError.serverMessage)
        else -> ChangePasswordError.Auth(toAuthError())
    }
}

private const val UNAUTHORIZED = 401
private const val REQUEST_TIMEOUT = 408