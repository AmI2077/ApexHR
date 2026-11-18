package com.example.apexhr.features.support.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.shop.ui.cart.composables.ShopSubscreenHeader
import com.example.apexhr.features.support.domain.SupportTicketStatus
import com.example.apexhr.features.support.ui.composables.SupportStatusPill
import com.example.apexhr.features.support.ui.composables.SupportTextField
import com.example.apexhr.features.support.ui.models.SupportMessageUiModel
import com.example.apexhr.features.support.ui.models.SupportTicketScreenCallbacks
import com.example.apexhr.features.support.ui.models.SupportTicketScreenState
import com.example.apexhr.features.support.ui.models.SupportTicketUiState
import com.example.apexhr.features.support.ui.models.closedNoteRes
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SupportTicketScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SupportTicketScreen(
            screenState = SupportTicketScreenState.Content(SupportTicketUiState.Preview),
            callbacks = SupportTicketScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SupportTicketScreenClosedPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SupportTicketScreen(
            screenState = SupportTicketScreenState.Content(
                SupportTicketUiState.Preview.copy(status = SupportTicketStatus.Resolved)
            ),
            callbacks = SupportTicketScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SupportTicketScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SupportTicketScreen(
            screenState = SupportTicketScreenState.Error(DataError.Server),
            callbacks = SupportTicketScreenCallbacks.Empty,
        )
    }
}

/**
 * Обращение: статус, переписка и поле ответа. У решённого и закрытого
 * обращения вместо поля ответа — пояснение.
 * @param messages ошибки отправки (id строки), показываются тостом.
 */
@Composable
fun SupportTicketScreen(
    screenState: SupportTicketScreenState,
    callbacks: SupportTicketScreenCallbacks,
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
            .background(backgroundColor)
            .imePadding(),
    ) {
        val uiState = (screenState as? SupportTicketScreenState.Content)?.uiState
        ShopSubscreenHeader(
            title = uiState?.subject ?: stringResource(R.string.support_untitled),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        when (screenState) {
            SupportTicketScreenState.Loading -> LoadingState()

            is SupportTicketScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is SupportTicketScreenState.Content -> SupportTicketContent(
                uiState = screenState.uiState,
                callbacks = callbacks,
            )
        }
    }
}

@Composable
private fun SupportTicketContent(
    uiState: SupportTicketUiState,
    callbacks: SupportTicketScreenCallbacks,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SupportStatusPill(
            status = uiState.status,
            modifier = Modifier.padding(horizontal = ApexDimens.spacing20.dp),
        )
        val listState = rememberLazyListState()
        // Новое сообщение — внизу, прокручиваем к нему.
        LaunchedEffect(uiState.messages.size) {
            if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.lastIndex)
        }
        if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(ApexDimens.spacing20.dp),
            ) {
                Text(
                    text = stringResource(R.string.support_no_messages),
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(ApexDimens.spacing20.dp),
                verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    SupportMessageCard(message = message)
                }
            }
        }
        val closedNote = uiState.status.closedNoteRes()
        if (uiState.canReply || closedNote == null) {
            SupportReplyBar(uiState = uiState, callbacks = callbacks)
        } else {
            Text(
                text = stringResource(closedNote),
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
                modifier = Modifier.padding(ApexDimens.spacing20.dp),
            )
        }
    }
}

@Composable
private fun SupportMessageCard(
    message: SupportMessageUiModel,
    modifier: Modifier = Modifier,
) {
    // Сообщения автора обращения — справа и голубые, ответы поддержки — слева.
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (message.isFromAuthor) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(ApexDimens.radius20.dp))
                .background(
                    if (message.isFromAuthor) secondaryBlueColor.copy(alpha = 0.28f) else pillBackgroundColor
                )
                .padding(ApexDimens.spacing12.dp),
            verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
        ) {
            Text(
                text = message.authorName ?: stringResource(R.string.support_author_fallback),
                color = primaryBlueColor,
                fontSize = ApexDimens.text12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = message.text, color = primaryTextColor, fontSize = ApexDimens.text14.sp)
            message.createdAt?.let {
                Text(text = it, color = secondaryTextColor, fontSize = ApexDimens.text12.sp)
            }
        }
    }
}

@Composable
private fun SupportReplyBar(
    uiState: SupportTicketUiState,
    callbacks: SupportTicketScreenCallbacks,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = ApexDimens.spacing20.dp,
                end = ApexDimens.spacing8.dp,
                top = ApexDimens.spacing8.dp,
                bottom = ApexDimens.spacing16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SupportTextField(
            value = uiState.reply,
            onValueChange = callbacks::onReplyChange,
            placeholder = stringResource(R.string.support_reply_placeholder),
            enabled = !uiState.isSending,
            maxLines = 4,
            modifier = Modifier.weight(1f),
        )
        if (uiState.isSending) {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = primaryBlueColor,
                    strokeWidth = ApexDimens.spacing2.dp,
                    modifier = Modifier.size(ApexDimens.icon20.dp),
                )
            }
        } else {
            IconButton(onClick = callbacks::onSendClick, enabled = uiState.canSend) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.support_send),
                    tint = if (uiState.canSend) primaryBlueColor else secondaryTextColor,
                )
            }
        }
    }
}