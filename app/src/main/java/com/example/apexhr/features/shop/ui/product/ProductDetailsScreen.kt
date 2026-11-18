package com.example.apexhr.features.shop.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.ApexPillTabs
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.activities.ui.common.ActivityActionButton
import com.example.apexhr.features.shop.ui.cart.composables.ShopSubscreenHeader
import com.example.apexhr.features.shop.ui.composables.ShopBadges
import com.example.apexhr.features.shop.ui.composables.ShopMessagesEffect
import com.example.apexhr.features.shop.ui.composables.ShopPrice
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.ShopMessageAction
import com.example.apexhr.features.shop.ui.product.models.ProductDetailsScreenCallbacks
import com.example.apexhr.features.shop.ui.product.models.ProductDetailsScreenState
import com.example.apexhr.features.shop.ui.product.models.ProductDetailsUiState
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ProductDetailsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ProductDetailsScreen(
            screenState = ProductDetailsScreenState.Content(ProductDetailsUiState.Preview),
            callbacks = ProductDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ProductDetailsScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ProductDetailsScreen(
            screenState = ProductDetailsScreenState.Error(DataError.Network),
            callbacks = ProductDetailsScreenCallbacks.Empty,
        )
    }
}

/**
 * Карточка товара, как в вебе: картинка, цена со скидкой, описание, выбор варианта,
 * наличие, формат (цифровой или физический) и «В корзину».
 * @param messages результат добавления в корзину (snackbar с кнопкой «Корзина»).
 */
@Composable
fun ProductDetailsScreen(
    screenState: ProductDetailsScreenState,
    callbacks: ProductDetailsScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<ShopMessage> = emptyFlow(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val cartLabel = stringResource(R.string.cart)
    ShopMessagesEffect(
        messages = messages,
        snackbarHostState = snackbarHostState,
        actionLabel = { message -> if (message.action == ShopMessageAction.OpenCart) cartLabel else null },
        onAction = { message -> if (message.action == ShopMessageAction.OpenCart) callbacks.onCartClick() },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ShopSubscreenHeader(
                title = stringResource(R.string.product_details_title),
                onBackClick = callbacks::onBackClick,
                modifier = Modifier.padding(ApexDimens.spacing8.dp),
                actions = {
                    IconButton(onClick = callbacks::onCartClick) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = stringResource(R.string.cart),
                            tint = primaryTextColor,
                        )
                    }
                },
            )
            when (screenState) {
                ProductDetailsScreenState.Loading -> LoadingState()
                is ProductDetailsScreenState.Error -> ErrorState(
                    error = screenState.error,
                    onRetry = callbacks::onRetryClick,
                )

                is ProductDetailsScreenState.Content -> ProductDetailsContent(screenState.uiState, callbacks)
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ProductDetailsContent(
    uiState: ProductDetailsUiState,
    callbacks: ProductDetailsScreenCallbacks,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ApexDimens.spacing20.dp, vertical = ApexDimens.spacing8.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(ApexDimens.radius20.dp))
                .background(secondaryBlueColor.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CardGiftcard,
                contentDescription = null,
                tint = primaryBlueColor,
                modifier = Modifier.size(ApexDimens.icon32.dp),
            )
            if (uiState.imageUrl != null) {
                AsyncImage(
                    model = uiState.imageUrl,
                    contentDescription = stringResource(R.string.shop_item_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        ShopBadges(badges = uiState.badges)
        Text(
            text = uiState.title.ifBlank { stringResource(R.string.shop_product_untitled) },
            color = primaryTextColor,
            fontSize = ApexDimens.text20.sp,
            fontWeight = FontWeight.Bold,
        )
        ShopPrice(
            price = uiState.price,
            originalPrice = uiState.originalPrice,
            discountPercent = uiState.discountPercent,
            fontSize = ApexDimens.text20.sp,
        )
        Text(
            text = uiState.description ?: stringResource(R.string.product_details_no_description),
            color = if (uiState.description == null) secondaryTextColor else primaryTextColor,
            fontSize = ApexDimens.text14.sp,
        )
        if (uiState.variants.isNotEmpty()) {
            Text(
                text = stringResource(R.string.product_details_variant),
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
            )
            val untitled = stringResource(R.string.product_details_variant_untitled)
            ApexPillTabs(
                labels = uiState.variants.map { it.name ?: untitled },
                selectedIndex = uiState.variants.indexOfFirst { it.id == uiState.selectedVariantId },
                onSelect = { index -> uiState.variants.getOrNull(index)?.let { callbacks.onVariantClick(it.id) } },
                enabled = !uiState.isAddingToCart,
            )
        }
        ProductDetailsRow(
            label = stringResource(R.string.product_details_stock),
            value = when {
                uiState.stock == null -> stringResource(R.string.shop_in_stock_unlimited)
                uiState.stock > 0 -> stringResource(R.string.product_details_stock_count, uiState.stock)
                else -> stringResource(R.string.shop_out_of_stock)
            },
        )
        ProductDetailsRow(
            label = stringResource(R.string.product_details_format),
            value = stringResource(
                if (uiState.isDigital) R.string.product_details_format_digital else R.string.product_details_format_physical,
            ),
        )
        ActivityActionButton(
            text = stringResource(if (uiState.isAvailable) R.string.shop_add_to_cart else R.string.shop_out_of_stock),
            inProgress = uiState.isAddingToCart,
            onClick = callbacks::onAddToCartClick,
            enabled = uiState.canAddToCart,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProductDetailsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = secondaryTextColor, fontSize = ApexDimens.text14.sp)
        Text(text = value, color = primaryTextColor, fontSize = ApexDimens.text14.sp, fontWeight = FontWeight.Medium)
    }
}