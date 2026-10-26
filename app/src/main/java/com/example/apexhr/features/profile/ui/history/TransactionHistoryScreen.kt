package com.example.apexhr.features.profile.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.composables.ApexPillTabs
import com.example.apexhr.core.ui.composables.EmptyState
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.profile.domain.TransactionFilter
import com.example.apexhr.features.profile.ui.history.models.HistoryTransactionUiModel
import com.example.apexhr.features.profile.ui.history.models.LoadMoreState
import com.example.apexhr.features.profile.ui.history.models.TransactionHistoryScreenCallbacks
import com.example.apexhr.features.profile.ui.history.models.TransactionHistoryScreenState
import com.example.apexhr.features.profile.ui.history.models.TransactionHistoryUiState
import com.example.apexhr.features.profile.ui.history.models.labelRes
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TransactionHistoryScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        TransactionHistoryScreen(
            screenState = TransactionHistoryScreenState.Content(TransactionHistoryUiState.Preview),
            callbacks = TransactionHistoryScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun TransactionHistoryScreenEmptyFilterPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        TransactionHistoryScreen(
            screenState = TransactionHistoryScreenState.Content(
                TransactionHistoryUiState.Preview.copy(
                    selectedFilter = TransactionFilter.Bonuses,
                    transactions = emptyList(),
                    loadMore = LoadMoreState.Error(DataError.Network),
                )
            ),
            callbacks = TransactionHistoryScreenCallbacks.Empty,
        )
    }
}

/** Полная история операций по балансу с фильтром по виду и подгрузкой страниц. */
@Composable
fun TransactionHistoryScreen(
    screenState: TransactionHistoryScreenState,
    callbacks: TransactionHistoryScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        HistoryHeader(
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        when (screenState) {
            TransactionHistoryScreenState.Loading -> LoadingState()

            is TransactionHistoryScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is TransactionHistoryScreenState.Content -> HistoryContent(
                uiState = screenState.uiState,
                callbacks = callbacks,
            )
        }
    }
}

@Composable
private fun HistoryContent(
    uiState: TransactionHistoryUiState,
    callbacks: TransactionHistoryScreenCallbacks,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HistoryFilters(
            selected = uiState.selectedFilter,
            onFilterClick = callbacks::onFilterClick,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ApexDimens.spacing20.dp,
                end = ApexDimens.spacing20.dp,
                top = ApexDimens.spacing8.dp,
                bottom = ApexDimens.spacing16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
        ) {
            if (uiState.transactions.isEmpty()) {
                item(key = "empty") {
                    EmptyState(text = stringResource(uiState.emptyTextRes))
                }
            }
            items(uiState.transactions, key = { it.id }) { transaction ->
                HistoryTransactionRow(transaction = transaction)
            }
            item(key = "load_more") {
                LoadMoreFooter(
                    state = uiState.loadMore,
                    onLoadMoreClick = callbacks::onLoadMoreClick,
                )
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
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
            text = stringResource(R.string.profile_history_title),
            color = primaryTextColor,
            fontSize = ApexDimens.text20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun HistoryFilters(
    selected: TransactionFilter,
    onFilterClick: (TransactionFilter) -> Unit,
) {
    val filters = TransactionFilter.entries
    ApexPillTabs(
        labels = filters.map { stringResource(it.labelRes()) },
        selectedIndex = filters.indexOf(selected),
        onSelect = { index -> onFilterClick(filters[index]) },
        contentPadding = PaddingValues(horizontal = ApexDimens.spacing20.dp),
    )
}

@Composable
private fun HistoryTransactionRow(
    transaction: HistoryTransactionUiModel,
    modifier: Modifier = Modifier,
) {
    val isPositive = transaction.amount >= 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(
                horizontal = ApexDimens.spacing16.dp,
                vertical = ApexDimens.spacing12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title.ifBlank { stringResource(R.string.profile_transaction_untitled) },
                color = primaryTextColor,
                fontSize = ApexDimens.text14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(stringResource(transaction.typeRes), transaction.date)
                    .joinToString(" · "),
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
            )
        }
        val amountColor = if (isPositive) primaryBlueColor else negativeRedColor
        ApexAmount(
            amount = transaction.amount,
            color = amountColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.SemiBold,
            iconTint = amountColor,
            showSign = true,
            modifier = Modifier.padding(start = ApexDimens.spacing12.dp),
        )
    }
}

@Composable
private fun LoadMoreFooter(
    state: LoadMoreState,
    onLoadMoreClick: () -> Unit,
) {
    when (state) {
        LoadMoreState.Hidden -> Unit

        LoadMoreState.Available -> OutlinedButton(
            onClick = onLoadMoreClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(ApexDimens.buttonHeight.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_history_load_more),
                color = primaryBlueColor,
                fontSize = ApexDimens.text14.sp,
            )
        }

        LoadMoreState.Loading -> LoadingState()

        is LoadMoreState.Error -> ErrorState(
            error = state.error,
            onRetry = onLoadMoreClick,
        )
    }
}
