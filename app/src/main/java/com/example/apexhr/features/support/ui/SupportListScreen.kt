package com.example.apexhr.features.support.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.apexhr.core.ui.composables.EmptyState
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadMoreFooter
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.activities.ui.common.ActivityActionButton
import com.example.apexhr.features.shop.ui.cart.composables.ShopSubscreenHeader
import com.example.apexhr.features.support.ui.composables.SupportStatusPill
import com.example.apexhr.features.support.ui.models.SupportListScreenCallbacks
import com.example.apexhr.features.support.ui.models.SupportListScreenState
import com.example.apexhr.features.support.ui.models.SupportListUiState
import com.example.apexhr.features.support.ui.models.SupportTicketItemUiModel
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SupportListScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SupportListScreen(
            screenState = SupportListScreenState.Content(SupportListUiState.Preview),
            callbacks = SupportListScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SupportListScreenEmptyPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SupportListScreen(
            screenState = SupportListScreenState.Content(SupportListUiState(emptyList(), hasNextPage = false)),
            callbacks = SupportListScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SupportListScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SupportListScreen(
            screenState = SupportListScreenState.Error(DataError.Network),
            callbacks = SupportListScreenCallbacks.Empty,
        )
    }
}

/**
 * «Поддержка»: кнопка нового обращения и список своих обращений.
 * Ошибка догрузки следующей страницы показывается в конце списка.
 */
@Composable
fun SupportListScreen(
    screenState: SupportListScreenState,
    callbacks: SupportListScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        ShopSubscreenHeader(
            title = stringResource(R.string.support_title),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        Column(
            modifier = Modifier.padding(
                start = ApexDimens.spacing20.dp,
                end = ApexDimens.spacing20.dp,
                bottom = ApexDimens.spacing16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
        ) {
            Text(
                text = stringResource(R.string.support_hint),
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
            )
            ActivityActionButton(
                text = stringResource(R.string.support_new_ticket),
                inProgress = false,
                onClick = callbacks::onNewTicketClick,
            )
        }
        when (screenState) {
            SupportListScreenState.Loading -> LoadingState()

            is SupportListScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is SupportListScreenState.Content -> if (screenState.uiState.items.isEmpty()) {
                EmptyState(text = stringResource(R.string.support_empty))
            } else {
                SupportTicketsList(uiState = screenState.uiState, callbacks = callbacks)
            }
        }
    }
}

@Composable
private fun SupportTicketsList(
    uiState: SupportListUiState,
    callbacks: SupportListScreenCallbacks,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ApexDimens.spacing20.dp,
            end = ApexDimens.spacing20.dp,
            bottom = ApexDimens.spacing16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
    ) {
        items(uiState.items, key = { it.id }) { ticket ->
            SupportTicketCard(ticket = ticket, onClick = { callbacks.onTicketClick(ticket.id) })
        }
        // Бесконечная прокрутка: следующая страница грузится у конца списка.
        item(key = "load_more") {
            LoadMoreFooter(state = uiState.loadMore, onLoadMore = callbacks::onLoadMoreClick)
        }
    }
}

@Composable
private fun SupportTicketCard(
    ticket: SupportTicketItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(pillBackgroundColor)
            .clickable(onClick = onClick)
            .padding(ApexDimens.spacing16.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
    ) {
        Text(
            text = ticket.subject ?: stringResource(R.string.support_untitled),
            color = primaryTextColor,
            fontSize = ApexDimens.text16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
        ) {
            SupportStatusPill(status = ticket.status)
            Text(
                text = stringResource(R.string.support_messages_count, ticket.messagesCount),
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
            )
            ticket.updatedAt?.let {
                Text(text = it, color = secondaryTextColor, fontSize = ApexDimens.text12.sp)
            }
        }
    }
}
