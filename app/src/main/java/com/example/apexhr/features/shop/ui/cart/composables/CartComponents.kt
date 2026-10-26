package com.example.apexhr.features.shop.ui.cart.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.composables.apexAmountText
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.shop.ui.cart.models.CartItemUiModel
import com.example.apexhr.features.shop.ui.cart.models.CartUiState

/** Шапка вложенного экрана каталога: «Назад», заголовок и [actions] справа. */
@Composable
fun ShopSubscreenHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = primaryTextColor,
            )
        }
        Text(
            text = title,
            color = primaryTextColor,
            fontSize = ApexDimens.text20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

@Composable
fun CartItemCard(
    item: CartItemUiModel,
    onIncreaseClick: () -> Unit,
    onDecreaseClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
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
            if (item.imageUrl == null) {
                Icon(
                    imageVector = Icons.Outlined.CardGiftcard,
                    contentDescription = null,
                    tint = primaryBlueColor,
                    modifier = Modifier.size(ApexDimens.icon24.dp),
                )
            } else {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = stringResource(R.string.shop_item_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        Spacer(modifier = Modifier.width(ApexDimens.spacing12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title.ifBlank { stringResource(R.string.shop_product_untitled) },
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ApexAmount(
                    amount = item.unitPrice,
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text12.sp,
                )
                Text(
                    text = stringResource(R.string.cart_item_per_piece),
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text12.sp,
                    modifier = Modifier.padding(start = ApexDimens.spacing4.dp),
                )
            }
            ApexAmount(
                amount = item.total,
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.Medium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecreaseClick,
                    enabled = item.canDecrease && !item.isUpdating,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = stringResource(R.string.cart_decrease),
                    )
                }
                Box(
                    modifier = Modifier.width(ApexDimens.icon32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(ApexDimens.icon16.dp),
                            color = primaryBlueColor,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = item.quantity.toString(),
                            color = primaryTextColor,
                            fontSize = ApexDimens.text16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                IconButton(
                    onClick = onIncreaseClick,
                    enabled = !item.isUpdating,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.cart_increase),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onRemoveClick,
                    enabled = !item.isUpdating,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.cart_remove),
                        tint = if (item.isUpdating) secondaryTextColor else negativeRedColor,
                    )
                }
            }
        }
    }
}

/** Пустая корзина: светлая карточка с подсказкой и кнопкой «В каталог». */
@Composable
fun CartEmptyCard(
    onCatalogClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(ApexDimens.spacing20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp),
    ) {
        Text(
            text = stringResource(R.string.cart_empty),
            color = primaryTextColor,
            fontSize = ApexDimens.text16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onCatalogClick,
            shape = RoundedCornerShape(ApexDimens.radius16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBlueColor,
                contentColor = backgroundColor,
            ),
        ) {
            Text(
                text = stringResource(R.string.cart_go_to_catalog),
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Итог корзины и баланс; если баланса не хватает — предупреждение. */
@Composable
fun CartSummaryCard(
    uiState: CartUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(ApexDimens.spacing16.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
    ) {
        CartSummaryRow(
            label = stringResource(R.string.cart_total),
            amount = uiState.total,
            isAccent = true,
        )
        CartSummaryRow(
            label = stringResource(R.string.cart_balance),
            amount = uiState.balance,
            isAccent = false,
        )
        if (!uiState.isBalanceEnough) {
            Text(
                // «Не хватает 1 апекса / 3 апексов / 25 апексов…»: склонение через plurals apex_amount.
                text = stringResource(R.string.cart_not_enough_balance, apexAmountText(uiState.missingAmount)),
                color = negativeRedColor,
                fontSize = ApexDimens.text13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun CartSummaryRow(
    label: String,
    amount: Long,
    isAccent: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = if (isAccent) primaryTextColor else secondaryTextColor,
            fontSize = ApexDimens.text14.sp,
        )
        ApexAmount(
            amount = amount,
            color = primaryTextColor,
            fontSize = if (isAccent) ApexDimens.text16.sp else ApexDimens.text14.sp,
            fontWeight = if (isAccent) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

/** «Оформить за ◇ N» (сумма — [ApexAmount]); во время оформления — индикатор. */
@Composable
fun CartCheckoutButton(
    uiState: CartUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = uiState.canCheckout,
        modifier = modifier
            .fillMaxWidth()
            .height(ApexDimens.buttonHeight.dp),
        shape = RoundedCornerShape(ApexDimens.radius16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryBlueColor,
            contentColor = backgroundColor,
        ),
    ) {
        if (uiState.isCheckoutInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(ApexDimens.icon20.dp),
                color = primaryBlueColor,
                strokeWidth = 2.dp,
            )
        } else {
            // Цвет берём из кнопки, чтобы сумма тускнела вместе с неактивной кнопкой.
            val contentColor = LocalContentColor.current
            Text(
                text = stringResource(R.string.cart_checkout),
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            ApexAmount(
                amount = uiState.total,
                modifier = Modifier.padding(start = ApexDimens.spacing6.dp),
                color = contentColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.SemiBold,
                iconTint = contentColor,
            )
        }
    }
}
