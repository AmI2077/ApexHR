package com.example.apexhr.features.shop.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.core.ui.theme.transparent
import com.example.apexhr.features.shop.ui.models.ShopProductUiModel

@Composable
fun ShopHeader(
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.shop_title),
            color = primaryTextColor,
            fontSize = ApexDimens.text28.sp,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onCartClick) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = stringResource(R.string.cart),
                tint = primaryTextColor,
                modifier = Modifier.size(ApexDimens.icon24.dp),
            )
        }
    }
}

@Composable
fun ShopSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(ApexDimens.searchFieldHeight.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.shop_search_placeholder),
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
            )
        },
        leadingIcon = {
//            Icon(
//                imageVector = Icons.Filled.Search,
//                contentDescription = stringResource(R.string.search),
//                tint = secondaryTextColor,
//                modifier = Modifier.size(ApexDimens.icon20.dp),
//            )
        },
        singleLine = true,
        shape = RoundedCornerShape(ApexDimens.radius20.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            unfocusedContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            disabledContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            focusedIndicatorColor = transparent,
            unfocusedIndicatorColor = transparent,
            disabledIndicatorColor = transparent,
            cursorColor = primaryBlueColor,
            focusedTextColor = primaryTextColor,
            unfocusedTextColor = primaryTextColor,
        ),
    )
}

@Composable
fun ShopPopularTitle(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.popular),
        color = primaryBlueColor,
        fontSize = ApexDimens.text12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

/**
 * Карточка товара: слева картинка (или заглушка), справа категория, название, описание,
 * цена, остаток и кнопка «В корзину».
 */
@Composable
fun ShopProductCard(
    product: ShopProductUiModel,
    onClick: () -> Unit,
    onGetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .clickable(onClick = onClick)
            .padding(ApexDimens.spacing12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(ApexDimens.productImageSize.dp)
                .clip(RoundedCornerShape(ApexDimens.radius16.dp))
                .background(secondaryBlueColor.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            if (product.showMark) {
                Icon(
                    imageVector = Icons.Outlined.CardGiftcard,
                    contentDescription = null,
                    tint = primaryBlueColor,
                    modifier = Modifier.size(ApexDimens.icon32.dp),
                )
            }
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = stringResource(R.string.shop_item_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        Spacer(modifier = Modifier.width(ApexDimens.spacing12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (product.category != null) {
                Text(
                    text = product.category,
                    color = primaryBlueColor,
                    fontSize = ApexDimens.text12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
            }
            Text(
                text = product.title.ifBlank { stringResource(R.string.shop_product_untitled) },
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (product.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
                Text(
                    text = product.subtitle,
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
            ApexAmount(
                amount = product.price,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
            Text(
                text = if (product.stock == null) {
                    stringResource(R.string.shop_in_stock_unlimited)
                } else {
                    stringResource(R.string.shop_in_stock, product.stock)
                },
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
            Button(
                onClick = onGetClick,
                enabled = product.isAvailable && !product.isAddingToCart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlueColor,
                    contentColor = backgroundColor,
                ),
                shape = RoundedCornerShape(ApexDimens.radius20.dp),
                contentPadding = PaddingValues(
                    horizontal = ApexDimens.spacing16.dp,
                    vertical = ApexDimens.spacing6.dp,
                ),
            ) {
                if (product.isAddingToCart) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ApexDimens.icon16.dp),
                        color = primaryBlueColor,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(ApexDimens.spacing6.dp))
                }
                Text(
                    text = stringResource(
                        if (product.isAvailable) R.string.shop_add_to_cart else R.string.shop_out_of_stock,
                    ),
                    fontSize = ApexDimens.text12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
