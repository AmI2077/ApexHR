package com.example.apexhr.features.auth.domain

/**
 * Причины, по которым не удалось войти или проверить сессию.
 * Для каждой показывается своё сообщение пользователю.
 */
sealed interface AuthError {
    /** Сервер ответил 400/401: неверный email или пароль. */
    data object InvalidCredentials : AuthError

    /** Сервер не ответил за отведённое время (например, холодный старт). */
    data object Timeout : AuthError

    /** Нет сети или сервер недоступен. */
    data object Network : AuthError

    /** Сервер вернул ошибку 5xx. */
    data object Server : AuthError

    /** Любая другая ошибка, в том числе неожиданный формат ответа. */
    data object Unknown : AuthError
}
