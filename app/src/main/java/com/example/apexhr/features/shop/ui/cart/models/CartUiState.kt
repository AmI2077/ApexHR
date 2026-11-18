package com.example.apexhr.features.shop.ui.cart.models

import com.example.apexhr.core.domain.DataError

data class CartUiState(
    val items: List<CartItemUiModel>,
    /** Сумма корзины в апексах. */
    val total: Long,
    val balance: Long,
    /** Хватает ли баланса на всю корзину. */
    val isBalanceEnough: Boolean,
    /** Сколько апексов не хватает; показывается, только если [isBalanceEnough] = false. */
    val missingAmount: Long,
    /** Идёт оформление заказа: кнопки корзины неактивны. */
    val isCheckoutInProgress: Boolean = false,
) {
    /** Оформить можно непустую корзину, если хватает баланса и нет других запросов. */
    val canCheckout: Boolean
        get() = items.isNotEmpty() &&
            isBalanceEnough &&
            !isCheckoutInProgress &&
            items.none { it.isUpdating }

    companion object {
        val Preview = CartUiState(
            items = listOf(
                CartItemUiModel(
                    id = 1,
                    title = "Худи Supabase",
                    imageUrl = null,
                    unitPrice = 1_990,
                    total = 3_980,
                    quantity = 2,
                    canDecrease = true,
                ),
                CartItemUiModel(
                    id = 2,
                    title = "Сертификат в книжный",
                    imageUrl = null,
                    unitPrice = 500,
                    total = 500,
                    quantity = 1,
                    canDecrease = false,
                ),
            ),
            total = 4_480,
            balance = 5_000,
            isBalanceEnough = true,
            missingAmount = 0,
        )
    }
}

data class CartItemUiModel(
    /** id позиции корзины. */
    val id: Long,
    val title: String,
    /** null — показываем значок вместо картинки. */
    val imageUrl: String?,
    /** Цена за штуку в апексах. */
    val unitPrice: Long,
    /** Сумма позиции (цена × количество) в апексах. */
    val total: Long,
    val quantity: Int,
    /** Уменьшать можно до 1 шт., дальше — только удалить. */
    val canDecrease: Boolean,
    /** По позиции идёт запрос: кнопки неактивны. */
    val isUpdating: Boolean = false,
    /** Название выбранного варианта товара; null — без варианта. */
    val variantName: String? = null,
)

sealed interface CartScreenState {
    data object Loading : CartScreenState
    data class Error(val error: DataError) : CartScreenState
    data class Content(val uiState: CartUiState) : CartScreenState
}
