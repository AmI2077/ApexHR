package com.example.apexhr.features.shop.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.shop.domain.CartActionError

/**
 * Разовое сообщение каталога или корзины (показывается в snackbar).
 * @param args аргументы строки [textRes].
 */
data class ShopMessage(
    @StringRes val textRes: Int,
    val args: List<Any> = emptyList(),
)

/** Текст ошибки действия с корзиной для пользователя. */
fun CartActionError.toShopMessage(): ShopMessage = when (this) {
    CartActionError.InsufficientBalance -> ShopMessage(R.string.cart_error_insufficient_balance)
    CartActionError.OutOfStock -> ShopMessage(R.string.cart_error_out_of_stock)
    CartActionError.EmptyCart -> ShopMessage(R.string.cart_error_empty)
    CartActionError.NotFound -> ShopMessage(R.string.cart_error_not_found)
    CartActionError.Forbidden -> ShopMessage(R.string.cart_error_forbidden)
    CartActionError.Conflict -> ShopMessage(R.string.cart_error_conflict)
    is CartActionError.Rejected -> if (serverMessage.isNullOrBlank()) {
        ShopMessage(R.string.cart_error_action)
    } else {
        // Текст сервера (по-английски) показываем как есть: другого объяснения нет.
        ShopMessage(R.string.cart_error_server_message, listOf(serverMessage))
    }
    // «Не удалось загрузить данные» не подходит к действию — свой общий текст.
    is CartActionError.Data -> if (error == DataError.Unknown) {
        ShopMessage(R.string.cart_error_action)
    } else {
        ShopMessage(error.messageRes())
    }
}
