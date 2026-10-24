package com.example.apexhr.core.domain

/**
 * Причины, по которым не удалось загрузить данные с сервера.
 * Общие для всех фич, кроме авторизации (у неё своя AuthError).
 */
sealed interface DataError {
    /** Сервер ответил 401. Сессию сбросит UnauthorizedHandler авторизованного клиента. */
    data object Unauthorized : DataError

    /** Сервер не ответил за отведённое время (например, холодный старт). */
    data object Timeout : DataError

    /** Нет сети или сервер недоступен. */
    data object Network : DataError

    /** Сервер вернул ошибку 5xx. */
    data object Server : DataError

    /** Любая другая ошибка, в том числе неожиданный формат ответа. */
    data object Unknown : DataError
}
