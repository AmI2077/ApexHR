package com.example.apexhr.features.shop.domain

import com.example.apexhr.core.domain.DataError

/**
 * Результат действия с корзиной (добавить, изменить, удалить, оформить).
 * В отличие от загрузки данных, у действий есть «бизнесовые» отказы сервера.
 */
sealed interface CartActionResult {
    data object Success : CartActionResult
    data class Failure(val error: CartActionError) : CartActionResult
}

/** Результат оформления заказа. */
sealed interface CheckoutResult {
    /** @param orderId номер созданного заказа; null, если сервер его не прислал. */
    data class Success(val orderId: Long?) : CheckoutResult
    data class Failure(val error: CartActionError) : CheckoutResult
}

sealed interface CartActionError {
    /** Сеть, таймаут, 401, 5xx — то же, что при загрузке данных. */
    data class Data(val error: DataError) : CartActionError

    /** Не хватает апексов на балансе. */
    data object InsufficientBalance : CartActionError

    /** Товар закончился или его меньше, чем запрошено. */
    data object OutOfStock : CartActionError

    /** Корзина пуста — оформлять нечего. */
    data object EmptyCart : CartActionError

    /** Позиция или товар уже не существуют (404). */
    data object NotFound : CartActionError

    /** Недостаточно прав (403). */
    data object Forbidden : CartActionError

    /** Конфликт (409): например, остаток или баланс изменились во время оформления. */
    data object Conflict : CartActionError

    /** Сервер отклонил запрос по другой причине; [serverMessage] — текст из ответа, если есть. */
    data class Rejected(val serverMessage: String?) : CartActionError
}
