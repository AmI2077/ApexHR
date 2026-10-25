package com.example.apexhr.features.activities.domain

import com.example.apexhr.core.domain.DataError

/**
 * Результат действия с активностью: ответ на анкету, участие в квесте,
 * регистрация на мероприятие, получение награды.
 */
sealed interface ActivityActionResult {
    data object Success : ActivityActionResult
    data class Failure(val error: ActivityActionError) : ActivityActionResult
}

sealed interface ActivityActionError {
    /** Сеть, таймаут, 401, 5xx — то же, что при загрузке данных. */
    data class Data(val error: DataError) : ActivityActionError

    /** Активность не найдена (404): её могли снять с публикации. */
    data object NotFound : ActivityActionError

    /** Недостаточно прав (403). */
    data object Forbidden : ActivityActionError

    /**
     * Сервер отклонил действие: 400 (проверка ответов) или 409 (закрыто,
     * нет мест, не выполнены шаги, посещение не подтверждено).
     * @param serverMessage текст из ответа (сервер пишет по-английски), если есть.
     */
    data class Rejected(val serverMessage: String?) : ActivityActionError
}
