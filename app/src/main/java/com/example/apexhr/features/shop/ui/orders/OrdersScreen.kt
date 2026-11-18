package com.example.apexhr.features.shop.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.composables.ApexPager
import com.example.apexhr.core.ui.composables.EmptyState
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.shop.ui.cart.composables.ShopSubscreenHeader
import com.example.apexhr.features.shop.ui.orders.models.OrderUiModel
import com.example.apexhr.features.shop.ui.orders.models.OrdersScreenCallbacks
import com.example.apexhr.features.shop.ui.orders.models.OrdersScreenState
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun OrdersScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        OrdersScreen(
            screenState = OrdersScreenState.Content(OrderUiModel.Preview),
            callbacks = OrdersScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun OrdersScreenEmptyPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        OrdersScreen(
            screenState = OrdersScreenState.Content(emptyList()),
            callbacks = OrdersScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun OrdersScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        OrdersScreen(
            screenState = OrdersScreenState.Error(DataError.Timeout),
            callbacks = OrdersScreenCallbacks.Empty,
        )
    }
}

/** «Мои заказы»: номер, дата, статус, состав и сумма каждого заказа. */
@Composable
fun OrdersScreen(
    screenState: OrdersScreenState,
    callbacks: OrdersScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        ShopSubscreenHeader(
            title = stringResource(R.string.orders_title),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        when (screenState) {
            OrdersScreenState.Loading -> LoadingState()

            is OrdersScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is OrdersScreenState.Content -> if (screenState.orders.isEmpty()) {
                EmptyState(text = stringResource(R.string.orders_empty))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = ApexDimens.spacing20.dp,
                        end = ApexDimens.spacing20.dp,
                        bottom = ApexDimens.spacing16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
                ) {
                    val slice = screenState.pageSlice
                    items(slice.items, key = { it.id }) { order ->
                        OrderCard(order = order)
                    }
                    item(key = "pager") {
                        ApexPager(slice = slice, onPageChange = callbacks::onPageChange)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(ApexDimens.spacing16.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.orders_number, order.id),
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = order.statusRes?.let { stringResource(it) }
                    ?: order.rawStatus
                    ?: stringResource(R.string.order_status_unknown),
                color = primaryBlueColor,
                fontSize = ApexDimens.text13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (order.createdAt != null) {
            Text(
                text = order.createdAt,
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
            )
        }
        order.items.forEach { item ->
            OrderAmountRow(
                label = stringResource(
                    R.string.orders_item,
                    item.title.ifBlank { stringResource(R.string.shop_product_untitled) }.let { title ->
                        if (item.variantName == null) title else stringResource(R.string.shop_title_with_variant, title, item.variantName)
                    },
                    item.quantity,
                ),
                amount = item.total,
                isTotal = false,
            )
        }
        OrderAmountRow(
            label = stringResource(R.string.orders_total),
            amount = order.total,
            isTotal = true,
        )
    }
}

/** Строка заказа: подпись слева, сумма в апексах справа. */
@Composable
private fun OrderAmountRow(
    label: String,
    amount: Long,
    isTotal: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = primaryTextColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = if (isTotal) FontWeight.SemiBold else null,
            modifier = Modifier
                .weight(1f)
                .padding(end = ApexDimens.spacing8.dp),
        )
        ApexAmount(
            amount = amount,
            fontSize = ApexDimens.text14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
