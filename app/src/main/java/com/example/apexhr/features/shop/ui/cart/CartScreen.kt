package com.example.apexhr.features.shop.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.EmptyState
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.features.shop.ui.cart.composables.CartCheckoutButton
import com.example.apexhr.features.shop.ui.cart.composables.CartItemCard
import com.example.apexhr.features.shop.ui.cart.composables.CartSummaryCard
import com.example.apexhr.features.shop.ui.cart.composables.ShopSubscreenHeader
import com.example.apexhr.features.shop.ui.cart.models.CartScreenCallbacks
import com.example.apexhr.features.shop.ui.cart.models.CartScreenState
import com.example.apexhr.features.shop.ui.cart.models.CartUiState
import com.example.apexhr.features.shop.ui.composables.ShopMessagesEffect
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.ShopMessageAction
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun CartScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        CartScreen(
            screenState = CartScreenState.Content(CartUiState.Preview),
            callbacks = CartScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun CartScreenNotEnoughBalancePreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        CartScreen(
            screenState = CartScreenState.Content(
                CartUiState.Preview.copy(
                    items = CartUiState.Preview.items.mapIndexed { index, item ->
                        item.copy(isUpdating = index == 1)
                    },
                    balance = "1\u00A0000",
                    isBalanceEnough = false,
                    missingAmount = "3\u00A0480",
                ),
            ),
            callbacks = CartScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun CartScreenCheckoutPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        CartScreen(
            screenState = CartScreenState.Content(
                CartUiState.Preview.copy(isCheckoutInProgress = true),
            ),
            callbacks = CartScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun CartScreenEmptyPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        CartScreen(
            screenState = CartScreenState.Content(
                CartUiState.Preview.copy(items = emptyList(), total = "0"),
            ),
            callbacks = CartScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun CartScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        CartScreen(
            screenState = CartScreenState.Error(DataError.Network),
            callbacks = CartScreenCallbacks.Empty,
        )
    }
}

/**
 * Корзина: позиции с количеством, итог, баланс и оформление заказа.
 * @param messages разовые сообщения (ошибки действий, итог оформления), показываются
 * в snackbar; у сообщения об оформленном заказе есть кнопка «Мои заказы».
 */
@Composable
fun CartScreen(
    screenState: CartScreenState,
    callbacks: CartScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<ShopMessage> = emptyFlow(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val ordersLabel = stringResource(R.string.cart_orders)
    ShopMessagesEffect(
        messages = messages,
        snackbarHostState = snackbarHostState,
        actionLabel = { message ->
            if (message.action == ShopMessageAction.OpenOrders) ordersLabel else null
        },
        onAction = { message ->
            if (message.action == ShopMessageAction.OpenOrders) callbacks.onOrdersClick()
        },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ShopSubscreenHeader(
                title = stringResource(R.string.cart),
                onBackClick = callbacks::onBackClick,
                modifier = Modifier.padding(
                    horizontal = ApexDimens.spacing8.dp,
                    vertical = ApexDimens.spacing8.dp,
                ),
                actions = {
                    TextButton(onClick = callbacks::onOrdersClick) {
                        Text(
                            text = ordersLabel,
                            color = primaryBlueColor,
                        )
                    }
                },
            )
            when (screenState) {
                CartScreenState.Loading -> LoadingState()

                is CartScreenState.Error -> ErrorState(
                    error = screenState.error,
                    onRetry = callbacks::onRetryClick,
                )

                is CartScreenState.Content -> if (screenState.uiState.items.isEmpty()) {
                    EmptyState(text = stringResource(R.string.cart_empty))
                } else {
                    CartContent(
                        uiState = screenState.uiState,
                        callbacks = callbacks,
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun CartContent(
    uiState: CartUiState,
    callbacks: CartScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ApexDimens.spacing20.dp,
            end = ApexDimens.spacing20.dp,
            bottom = ApexDimens.spacing16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
    ) {
        items(uiState.items, key = { it.id }) { item ->
            CartItemCard(
                item = item,
                onIncreaseClick = { callbacks.onIncreaseClick(item.id) },
                onDecreaseClick = { callbacks.onDecreaseClick(item.id) },
                onRemoveClick = { callbacks.onRemoveClick(item.id) },
            )
        }
        item {
            CartSummaryCard(uiState = uiState)
        }
        item {
            CartCheckoutButton(
                uiState = uiState,
                onClick = callbacks::onCheckoutClick,
            )
        }
    }
}
