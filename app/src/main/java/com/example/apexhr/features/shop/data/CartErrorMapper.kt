package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.toDataError
import com.example.apexhr.features.shop.domain.CartActionError
import kotlinx.coroutines.CancellationException

/**
 * Отказ сервера -> доменная ошибка. Коды 400 у бэкенда общие для разных причин,
 * поэтому сначала смотрим на текст ошибки (сервер пишет по-английски, например
 * «Requested quantity exceeds available stock.»), потом на код.
 */
fun CartApiException.toActionError(): CartActionError {
    if (statusCode == 401 || statusCode == 408 || statusCode >= 500) {
        return CartActionError.Data(ApiStatusException(statusCode).toDataError())
    }

    val text = serverMessage.orEmpty().lowercase()
    return when {
        text.containsAny(STOCK_KEYWORDS) -> CartActionError.OutOfStock
        text.containsAny(BALANCE_KEYWORDS) -> CartActionError.InsufficientBalance
        text.containsAny(EMPTY_CART_KEYWORDS) -> CartActionError.EmptyCart
        statusCode == 403 -> CartActionError.Forbidden
        statusCode == 404 -> CartActionError.NotFound
        statusCode == 409 -> CartActionError.Conflict
        else -> CartActionError.Rejected(serverMessage)
    }
}

/** Любое исключение действия -> доменная ошибка. */
fun Throwable.toCartActionError(): CartActionError =
    if (this is CartApiException) toActionError() else CartActionError.Data(toDataError())

/** Выполняет действие; отмену корутины не перехватывает. */
internal suspend fun <T> safeCartAction(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

private fun String.containsAny(keywords: List<String>) = keywords.any { contains(it) }

// Склад проверяется раньше баланса: «Insufficient stock» тоже начинается с «insufficient».
// Корень «остат» не подходит: он есть в «недостаточно».
private val STOCK_KEYWORDS = listOf("stock", "unavailable", "наличи", "остаток", "остатк", "склад")
private val BALANCE_KEYWORDS = listOf("balance", "funds", "insufficient", "баланс", "средств", "апекс")
private val EMPTY_CART_KEYWORDS = listOf("empty", "пуст")
