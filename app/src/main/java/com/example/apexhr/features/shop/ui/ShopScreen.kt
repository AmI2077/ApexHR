package com.example.apexhr.features.shop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.features.shop.ui.composables.ShopCategoryTabs
import com.example.apexhr.features.shop.ui.composables.ShopHeader
import com.example.apexhr.features.shop.ui.composables.ShopPopularTitle
import com.example.apexhr.features.shop.ui.composables.ShopProductCard
import com.example.apexhr.features.shop.ui.composables.ShopSearchField
import com.example.apexhr.features.shop.ui.models.ShopScreenCallbacks
import com.example.apexhr.features.shop.ui.models.ShopUiState
import com.example.apexhr.ui.theme.ApexHRTheme

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
