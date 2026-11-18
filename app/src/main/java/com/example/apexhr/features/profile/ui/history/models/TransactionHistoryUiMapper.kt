package com.example.apexhr.features.profile.ui.history.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.profile.domain.HistoryTransaction
import com.example.apexhr.features.profile.domain.TransactionFilter
import com.example.apexhr.features.profile.domain.TransactionType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val HISTORY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

/**
 * @param isLoadingMore идёт загрузка следующей страницы.
 * @param loadMoreError ошибка последней попытки загрузить следующую страницу.
 */
fun LoadedTransactions.toUiState(
    filter: TransactionFilter,
    isLoadingMore: Boolean = false,
    loadMoreError: DataError? = null,
    zone: ZoneId = ZoneId.systemDefault(),
): TransactionHistoryUiState = TransactionHistoryUiState(
    selectedFilter = filter,
    transactions = items
        .filter { filter.matches(it.type) }
        .map { it.toUiModel(zone) },
    emptyTextRes = when {
        items.isEmpty() -> R.string.profile_history_empty
        hasMore -> R.string.profile_history_filter_empty_loaded
        else -> R.string.profile_history_filter_empty
    },
    loadMore = when {
        isLoadingMore -> LoadMoreState.Loading
        !hasMore -> LoadMoreState.Hidden
        loadMoreError != null -> LoadMoreState.Error(loadMoreError)
        else -> LoadMoreState.Available
    },
)

fun HistoryTransaction.toUiModel(zone: ZoneId = ZoneId.systemDefault()) = HistoryTransactionUiModel(
    id = id.toString(),
    title = description.orEmpty(),
    typeRes = type.labelRes(),
    date = createdAt?.formatHistoryDate(zone),
    amount = amount,
)

fun Instant.formatHistoryDate(zone: ZoneId = ZoneId.systemDefault()): String =
    HISTORY_DATE_FORMAT.format(atZone(zone))

@StringRes
fun TransactionFilter.labelRes(): Int = when (this) {
    TransactionFilter.All -> R.string.profile_history_filter_all
    TransactionFilter.Activities -> R.string.profile_history_filter_activities
    TransactionFilter.Thanks -> R.string.profile_history_filter_thanks
    TransactionFilter.Bonuses -> R.string.profile_history_filter_bonuses
    TransactionFilter.Purchases -> R.string.profile_history_filter_purchases
    TransactionFilter.Refunds -> R.string.profile_history_filter_refunds
    TransactionFilter.Adjustments -> R.string.profile_history_filter_adjustments
}

@StringRes
fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.Activity -> R.string.profile_history_type_activity
    TransactionType.Thanks -> R.string.profile_history_type_thanks
    TransactionType.Bonus -> R.string.profile_history_type_bonus
    TransactionType.Purchase -> R.string.profile_history_type_purchase
    TransactionType.Refund -> R.string.profile_history_type_refund
    TransactionType.Adjustment -> R.string.profile_history_type_adjustment
    TransactionType.Other -> R.string.profile_history_type_other
}
