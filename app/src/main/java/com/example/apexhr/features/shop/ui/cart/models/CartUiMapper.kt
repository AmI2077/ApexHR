package com.example.apexhr.features.shop.ui.cart.models

import com.example.apexhr.core.ui.model.formatAmount
import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.shop.domain.CartItem
import com.example.apexhr.features.shop.domain.CartSummary

/** @param updatingItemIds позиции, по которым сейчас идёт запрос. */
fun CartSummary.toUiState(updatingItemIds: Set<Long> = emptySet()): CartUiState =
    CartUiState(
        items = cart.items.map { it.toUiModel(isUpdating = it.id in updatingItemIds) },
        total = formatAmount(cart.total),
        balance = formatAmount(balance),
        isBalanceEnough = isBalanceEnough,
        missingAmount = formatAmount(missingAmount),
    )

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
