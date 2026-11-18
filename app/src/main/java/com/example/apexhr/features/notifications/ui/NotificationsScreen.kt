package com.example.apexhr.features.notifications.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.notifications.ui.models.NotificationUiModel
import com.example.apexhr.features.notifications.ui.models.NotificationsScreenCallbacks
import com.example.apexhr.features.notifications.ui.models.NotificationsScreenState
import com.example.apexhr.features.notifications.ui.models.NotificationsUiState
import com.example.apexhr.features.shop.ui.cart.composables.ShopSubscreenHeader
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun NotificationsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NotificationsScreen(
            screenState = NotificationsScreenState.Content(NotificationsUiState.Preview),
            callbacks = NotificationsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun NotificationsScreenEmptyPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NotificationsScreen(
            screenState = NotificationsScreenState.Content(
                NotificationsUiState(items = emptyList(), hasNextPage = false)
            ),
            callbacks = NotificationsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun NotificationsScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NotificationsScreen(
            screenState = NotificationsScreenState.Error(DataError.Timeout),
            callbacks = NotificationsScreenCallbacks.Empty,
        )
    }
}

/**
 * Уведомления: непрочитанные выделены точкой и жирным заголовком,
 * нажатие отмечает уведомление прочитанным.
 * @param messages ошибки действий (id строки), показываются тостом.
 */
@Composable
fun NotificationsScreen(
    screenState: NotificationsScreenState,
    callbacks: NotificationsScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<Int> = emptyFlow(),
) {
    val context = LocalContext.current
    LaunchedEffect(messages) {
        messages.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        ShopSubscreenHeader(
            title = stringResource(R.string.notifications_title),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        ) {
            val uiState = (screenState as? NotificationsScreenState.Content)?.uiState
            if (uiState != null && uiState.hasUnread) {
                TextButton(
                    onClick = callbacks::onReadAllClick,
                    enabled = !uiState.isMarkingAll,
                ) {
                    Text(
                        text = stringResource(R.string.notifications_read_all),
                        color = primaryBlueColor,
                        fontSize = ApexDimens.text14.sp,
                    )
                }
            }
        }
        when (screenState) {
            NotificationsScreenState.Loading -> LoadingState()

            is NotificationsScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is NotificationsScreenState.Content -> if (screenState.uiState.items.isEmpty()) {
                EmptyState(text = stringResource(R.string.notifications_empty))
            } else {
                NotificationsList(uiState = screenState.uiState, callbacks = callbacks)
            }
        }
    }
}

@Composable
private fun NotificationsList(
    uiState: NotificationsUiState,
    callbacks: NotificationsScreenCallbacks,
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
        items(uiState.items, key = { it.id }) { notification ->
            NotificationCard(
                notification = notification,
                onClick = { callbacks.onNotificationClick(notification.id) },
            )
        }
        // Бесконечная прокрутка: следующая страница грузится у конца списка.
        item(key = "load_more") {
            LoadMoreFooter(state = uiState.loadMore, onLoadMore = callbacks::onLoadMoreClick)
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unreadLabel = stringResource(R.string.notifications_unread)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(
                if (notification.isRead) pillBackgroundColor else secondaryBlueColor.copy(alpha = 0.28f)
            )
            .clickable(onClick = onClick)
            .padding(ApexDimens.spacing16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
        ) {
            Text(
                text = notification.title ?: stringResource(R.string.notifications_untitled),
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
            )
            notification.body?.let {
                Text(
                    text = it,
                    color = primaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                )
            }
            notification.createdAt?.let {
                Text(
                    text = it,
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text12.sp,
                )
            }
        }
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .padding(start = ApexDimens.spacing8.dp, top = ApexDimens.spacing4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(primaryBlueColor)
                    .semantics { contentDescription = unreadLabel },
            )
        }
    }
}
