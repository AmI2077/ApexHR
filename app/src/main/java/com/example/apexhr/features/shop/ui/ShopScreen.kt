package com.example.apexhr.features.shop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.apexhr.features.shop.ui.composables.ShopCategoryTabs
import com.example.apexhr.features.shop.ui.composables.ShopHeader
import com.example.apexhr.features.shop.ui.composables.ShopMessagesEffect
import com.example.apexhr.features.shop.ui.composables.ShopPopularTitle
import com.example.apexhr.features.shop.ui.composables.ShopProductCard
import com.example.apexhr.features.shop.ui.composables.ShopSearchField
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.ShopScreenCallbacks
import com.example.apexhr.features.shop.ui.models.ShopScreenState
import com.example.apexhr.features.shop.ui.models.ShopUiState
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ShopScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ShopScreen(
            uiState = ShopUiState.Preview,
            callbacks = ShopScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ShopScreenLoadingPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ShopScreen(
            screenState = ShopScreenState.Loading,
            callbacks = ShopScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ShopScreenProductsErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ShopScreen(
            uiState = ShopUiState.Preview.copy(
                products = emptyList(),
                productsError = DataError.Network,
            ),
            callbacks = ShopScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ShopScreenAddingToCartPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ShopScreen(
            uiState = ShopUiState.Preview.copy(
                products = ShopUiState.Preview.products.mapIndexed { index, product ->
                    product.copy(isAddingToCart = index == 0)
                },
            ),
            callbacks = ShopScreenCallbacks.Empty,
        )
    }
}

/**
 * @param messages разовые сообщения (добавление в корзину), показываются в snackbar.
 */
@Composable
fun ShopScreen(
    screenState: ShopScreenState,
    callbacks: ShopScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<ShopMessage> = emptyFlow(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShopMessagesEffect(
        messages = messages,
        snackbarHostState = snackbarHostState,
    )

    Box(modifier = modifier.fillMaxSize()) {
        when (screenState) {
            is ShopScreenState.Content -> ShopScreenContent(
                uiState = screenState.uiState,
                callbacks = callbacks,
            )

            ShopScreenState.Loading -> ShopScreenFrame(callbacks) {
                item { LoadingState() }
            }

            is ShopScreenState.Error -> ShopScreenFrame(callbacks) {
                item {
                    ErrorState(
                        error = screenState.error,
                        onRetry = callbacks::onRetryClick,
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
fun ShopScreen(
    uiState: ShopUiState,
    callbacks: ShopScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    ShopScreenContent(
        uiState = uiState,
        callbacks = callbacks,
        modifier = modifier,
    )
}

@Composable
fun ShopScreenContent(
    uiState: ShopUiState,
    callbacks: ShopScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    ShopScreenFrame(callbacks, modifier) {
        item {
            ShopSearchField(
                query = uiState.searchQuery,
                onQueryChange = callbacks::onSearchQueryChange,
            )
        }
        item {
            ShopCategoryTabs(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategoryClick = callbacks::onCategoryClick,
            )
        }
        when {
            uiState.isProductsLoading -> item { LoadingState() }

            uiState.productsError != null -> item {
                ErrorState(
                    error = uiState.productsError,
                    onRetry = callbacks::onRetryClick,
                )
            }

            uiState.products.isEmpty() -> item {
                val text = if (uiState.searchQuery.isBlank()) {
                    R.string.shop_products_empty
                } else {
                    R.string.shop_search_empty
                }
                EmptyState(text = stringResource(text))
            }

            else -> {
                item {
                    ShopPopularTitle()
                }
                items(uiState.products, key = { it.id }) { product ->
                    ShopProductCard(
                        product = product,
                        onClick = { callbacks.onProductClick(product.id) },
                        onGetClick = { callbacks.onGetProductClick(product.id) },
                    )
                }
            }
        }
    }
}

/** Общий каркас: фон, отступы и шапка с корзиной. */
@Composable
private fun ShopScreenFrame(
    callbacks: ShopScreenCallbacks,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentPadding = PaddingValues(
            horizontal = ApexDimens.spacing20.dp,
            vertical = ApexDimens.spacing16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp),
    ) {
        item {
            ShopHeader(onCartClick = callbacks::onCartClick)
        }
        content()
    }
}
