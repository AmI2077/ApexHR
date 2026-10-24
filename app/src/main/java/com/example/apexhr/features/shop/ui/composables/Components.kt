package com.example.apexhr.features.shop.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.core.ui.theme.transparent
import com.example.apexhr.features.shop.ui.models.ShopCategoryUiModel
import com.example.apexhr.features.shop.ui.models.ShopProductUiModel
import com.example.apexhr.features.shop.ui.models.ShopUiState

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
//            Icon(
//                imageVector = Icons.Outlined.ShoppingCart,
//                contentDescription = stringResource(R.string.cart),
//                tint = primaryTextColor,
//                modifier = Modifier.size(ApexDimens.icon24.dp),
//            )
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
fun ShopCategoryTabs(
    categories: List<ShopCategoryUiModel>,
    selectedCategoryId: String,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Категорий с сервера может быть больше, чем помещается в ширину экрана.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing24.dp),
    ) {
        categories.forEach { category ->
            val selected = category.id == selectedCategoryId
            val title = when {
                category.id == ShopUiState.CATEGORY_ALL -> stringResource(R.string.shop_category_all)
                category.title.isNullOrBlank() -> stringResource(R.string.shop_category_untitled)
                else -> category.title
            }
            Text(
                text = title,
                color = if (selected) primaryBlueColor else secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.clickable { onCategoryClick(category.id) },
            )
        }
    }
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ApexDimens.productImageSize.dp)
                .clip(RoundedCornerShape(ApexDimens.radius16.dp))
                .background(secondaryBlueColor.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            if (product.showMark) {
                Text(
                    text = stringResource(R.string.points_diamond),
                    color = primaryBlueColor,
                    fontSize = ApexDimens.text24.sp,
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
            Text(
                text = product.title.ifBlank { stringResource(R.string.shop_product_untitled) },
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
            Text(
                text = product.subtitle,
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.shop_price, product.price),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onGetClick,
                    enabled = product.isAvailable,
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
                    Text(
                        text = stringResource(
                            if (product.isAvailable) R.string.get else R.string.shop_out_of_stock,
                        ),
                        fontSize = ApexDimens.text12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
