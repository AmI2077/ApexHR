package com.example.apexhr.features.shop.ui.cart.models

import com.example.apexhr.core.domain.DataError

data class CartUiState(
    val items: List<CartItemUiModel>,
    /** Сумма корзины, уже отформатированная. */
    val total: String,
    val balance: String,
    /** Хватает ли баланса на всю корзину. */
    val isBalanceEnough: Boolean,
    /** Сколько апексов не хватает; показывается, только если [isBalanceEnough] = false. */
    val missingAmount: String,
) {
    companion object {
        val Preview = CartUiState(
            items = listOf(
                CartItemUiModel(
                    id = 1,
                    title = "Худи Supabase",
                    imageUrl = null,
                    unitPrice = "1\u00A0990",
                    total = "3\u00A0980",
                    quantity = 2,
                    canDecrease = true,
                ),
                CartItemUiModel(
                    id = 2,
                    title = "Сертификат в книжный",
                    imageUrl = null,
                    unitPrice = "500",
                    total = "500",
                    quantity = 1,
                    canDecrease = false,
                ),
            ),
            total = "4\u00A0480",
            balance = "5\u00A0000",
            isBalanceEnough = true,
            missingAmount = "0",
        )
    }
}

data class CartItemUiModel(
    /** id позиции корзины. */
    val id: Long,
    val title: String,
    /** null — показываем значок вместо картинки. */
    val imageUrl: String?,
    val unitPrice: String,
    val total: String,
    val quantity: Int,
    /** Уменьшать можно до 1 шт., дальше — только удалить. */
    val canDecrease: Boolean,
    /** По позиции идёт запрос: кнопки неактивны. */
    val isUpdating: Boolean = false,
)

sealed interface CartScreenState {
    data object Loading : CartScreenState
    data class Error(val error: DataError) : CartScreenState
    data class Content(val uiState: CartUiState) : CartScreenState
}
