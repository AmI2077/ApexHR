package com.example.apexhr.features.thanks.domain

import com.example.apexhr.core.domain.DataError

/** Результат отправки благодарности. */
sealed interface ThanksSendResult {
    data object Success : ThanksSendResult
    data class Failure(val error: ThanksSendError) : ThanksSendResult
}

sealed interface ThanksSendError {
    /** Сеть, таймаут, 401, 5xx — то же, что при загрузке данных. */
    data class Data(val error: DataError) : ThanksSendError

    /** Получатель не найден (404). */
    data object NotFound : ThanksSendError

    /** Недостаточно прав (403). */
    data object Forbidden : ThanksSendError

    /**
     * Сервер отклонил благодарность (400 и прочие 4xx), например исчерпан
     * недельный лимит.
     * @param serverMessage текст из ответа, если есть.
     */
    data class Rejected(val serverMessage: String?) : ThanksSendError
}
