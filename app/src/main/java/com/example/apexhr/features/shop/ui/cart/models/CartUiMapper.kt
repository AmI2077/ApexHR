package com.example.apexhr.features.shop.ui.cart.models

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.formatAmount
import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.shop.domain.CartActionError
import com.example.apexhr.features.shop.domain.CartItem
import com.example.apexhr.features.shop.domain.CartSummary
import com.example.apexhr.features.shop.domain.CheckoutResult
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.ShopMessageAction
import com.example.apexhr.features.shop.ui.models.toShopMessage

/**
 * @param updatingItemIds позиции, по которым сейчас идёт запрос.
 * @param isCheckoutInProgress идёт оформление заказа.
 */
fun CartSummary.toUiState(
    updatingItemIds: Set<Long> = emptySet(),
    isCheckoutInProgress: Boolean = false,
): CartUiState =
    CartUiState(
        items = cart.items.map { it.toUiModel(isUpdating = it.id in updatingItemIds) },
        total = formatAmount(cart.total),
        balance = formatAmount(balance),
        isBalanceEnough = isBalanceEnough,
        missingAmount = formatAmount(missingAmount),
        isCheckoutInProgress = isCheckoutInProgress,
    )

/** Сообщение после оформления заказа. */
fun CheckoutResult.toShopMessage(): ShopMessage = when (this) {
    is CheckoutResult.Success -> if (orderId != null) {
        ShopMessage(
            textRes = R.string.cart_checkout_success,
            args = listOf(orderId),
            action = ShopMessageAction.OpenOrders,
        )
    } else {
        ShopMessage(
            textRes = R.string.cart_checkout_success_no_number,
            action = ShopMessageAction.OpenOrders,
        )
    }
    // Ответа не дождались, но заказ мог создаться: повторять вслепую нельзя.
    is CheckoutResult.Failure -> if (error == CartActionError.Data(DataError.Timeout)) {
        ShopMessage(R.string.cart_checkout_timeout)
    } else {
        error.toShopMessage()
    }
}

fun CartItem.toUiModel(isUpdating: Boolean = false): CartItemUiModel =
    CartItemUiModel(
        id = id,
        title = name.orEmpty(),
        imageUrl = imageUrl.takeIfHttpUrl(),
        unitPrice = formatAmount(unitPrice),
        total = formatAmount(total),
        quantity = quantity,
        canDecrease = quantity > 1,
        isUpdating = isUpdating,
    )
