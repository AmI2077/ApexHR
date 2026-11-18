package com.example.apexhr.features.thanks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.EmptyState
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.composables.apexAmountText
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.features.thanks.ui.composables.ColleagueRow
import com.example.apexhr.features.thanks.ui.composables.SendThanksButton
import com.example.apexhr.features.thanks.ui.composables.ThanksHeader
import com.example.apexhr.features.thanks.ui.composables.ThanksMessageCounter
import com.example.apexhr.features.thanks.ui.composables.ThanksMessageField
import com.example.apexhr.features.thanks.ui.composables.ThanksNoticeText
import com.example.apexhr.features.thanks.ui.composables.ThanksSearchField
import com.example.apexhr.features.thanks.ui.composables.ThanksSectionLabel
import com.example.apexhr.features.thanks.ui.models.ThanksScreenCallbacks
import com.example.apexhr.features.thanks.ui.models.ThanksScreenState
import com.example.apexhr.features.thanks.ui.models.ThanksUiState
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ThanksScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ThanksScreen(
            screenState = ThanksScreenState.Content(ThanksUiState.Preview),
            callbacks = ThanksScreenCallbacks.Empty,
        )
    }
}

/** Отправка благодарности коллеге; история благодарностей — в профиле. */
@Composable
fun ThanksScreen(
    screenState: ThanksScreenState,
    callbacks: ThanksScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    when (screenState) {
        ThanksScreenState.Loading -> ThanksScreenFrame(callbacks, modifier) {
            LoadingState()
        }

        is ThanksScreenState.Error -> ThanksScreenFrame(callbacks, modifier) {
            ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )
        }

        is ThanksScreenState.Content -> ThanksScreenContent(
            uiState = screenState.uiState,
            callbacks = callbacks,
            modifier = modifier,
        )
    }
}

@Composable
fun ThanksScreenContent(
    uiState: ThanksUiState,
    callbacks: ThanksScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    ThanksScreenFrame(callbacks, modifier) {
        Text(
            text = stringResource(R.string.thanks_rules, apexAmountText(uiState.amount)),
            color = secondaryTextColor,
            fontSize = ApexDimens.text13.sp,
        )
        ThanksSectionLabel(text = stringResource(R.string.thanks_colleagues))
        ThanksSearchField(
            query = uiState.searchQuery,
            onQueryChange = callbacks::onSearchQueryChange,
        )
        if (uiState.colleagues.isEmpty()) {
            EmptyState(
                text = stringResource(
                    if (uiState.isNothingFound) R.string.thanks_nothing_found else R.string.thanks_no_colleagues
                ),
            )
        }
        uiState.colleagues.forEach { colleague ->
            ColleagueRow(
                colleague = colleague,
                selected = colleague.id == uiState.selectedColleagueId,
                onClick = { callbacks.onColleagueClick(colleague.id) },
                onQuickThanksClick = { callbacks.onQuickThanksClick(colleague.id) },
                onProfileClick = { callbacks.onColleagueProfileClick(colleague.id) },
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.thanks_send_section),
                color = primaryTextColor,
                fontSize = ApexDimens.text18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
            if (uiState.selectedColleagueId != null) {
                val name = uiState.selectedColleagueName
                    ?: stringResource(R.string.thanks_unknown_colleague)
                Text(
                    text = stringResource(R.string.thanks_recipient, name),
                    color = primaryBlueColor,
                    fontSize = ApexDimens.text14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
                Text(
                    text = if (uiState.isSelectedThankedRecently) {
                        stringResource(R.string.thanks_weekly_limit_reached)
                    } else {
                        stringResource(R.string.thanks_amount_hint, apexAmountText(uiState.amount))
                    },
                    color = if (uiState.isSelectedThankedRecently) negativeRedColor else secondaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            ThanksMessageField(
                message = uiState.message,
                onMessageChange = callbacks::onMessageChange,
            )
            ThanksMessageCounter(
                length = uiState.message.length,
                maxLength = uiState.messageMaxLength,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = ApexDimens.spacing4.dp),
            )
        }
        uiState.notice?.let { notice -> ThanksNoticeText(notice = notice) }
        SendThanksButton(
            onClick = callbacks::onSendClick,
            enabled = uiState.canSend,
        )
        if (uiState.isSending) {
            Text(
                text = stringResource(R.string.thanks_sending),
                color = primaryBlueColor,
                fontSize = ApexDimens.text14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
    }
}

/** Общий каркас: фон, прокрутка, отступы и шапка. */
@Composable
private fun ThanksScreenFrame(
    callbacks: ThanksScreenCallbacks,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = ApexDimens.spacing20.dp,
                vertical = ApexDimens.spacing8.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp),
    ) {
        ThanksHeader(onBackClick = callbacks::onBackClick)
        content()
    }
}
