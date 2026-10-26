package com.example.apexhr.features.auth.domain

sealed interface ChangePasswordResult {
    data object Success : ChangePasswordResult
    data class Failure(val error: ChangePasswordError) : ChangePasswordResult
}

sealed interface ChangePasswordError {
    /**
     * Сервер отклонил новый пароль (400 и прочие 4xx).
     * @param serverMessage текст из ответа, если есть.
     */
    data class Rejected(val serverMessage: String?) : ChangePasswordError

    /** Сервер ответил 401: сессия закончилась, нужно войти заново. */
    data object SessionExpired : ChangePasswordError

    /** Сеть, таймаут, 5xx и прочее — как при входе. */
    data class Auth(val error: AuthError) : ChangePasswordError
}