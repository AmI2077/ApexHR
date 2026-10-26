package com.example.apexhr.features.profile.ui.history.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.profile.domain.TransactionFilter

data class TransactionHistoryUiState(
    val selectedFilter: TransactionFilter,
    /** Загруженные операции, подходящие под фильтр. */
    val transactions: List<HistoryTransactionUiModel>,
    /** Текст, если [transactions] пуст: операций нет совсем или нет под фильтр среди загруженных/всех. */
    @StringRes val emptyTextRes: Int,
    val loadMore: LoadMoreState,
) {
    companion object {
        val Preview = TransactionHistoryUiState(
            selectedFilter = TransactionFilter.All,
            transactions = listOf(
                HistoryTransactionUiModel(
                    id = "11",
                    title = "Заказ от 2026-10-25 20:16:44",
                    typeRes = R.string.profile_history_type_purchase,
                    date = "25.10.2026 23:16",
                    amount = -50,
                ),
                HistoryTransactionUiModel(
                    id = "10",
                    title = "Спасибо за помощь с релизом",
                    typeRes = R.string.profile_history_type_thanks,
                    date = "25.10.2026 21:55",
                    amount = 20,
                ),
                HistoryTransactionUiModel(
                    id = "9",
                    title = "Получено достижение \"Первое мероприятие\"",
                    typeRes = R.string.profile_history_type_activity,
                    date = "25.10.2026 17:40",
                    amount = 10,
                ),
            ),
            emptyTextRes = R.string.profile_history_filter_empty_loaded,
            loadMore = LoadMoreState.Available,
        )
    }
}

data class HistoryTransactionUiModel(
    val id: String,
    /** Описание с сервера; пустое — показываем название по умолчанию. */
    val title: String,
    @StringRes val typeRes: Int,
    /** Дата и время в часовом поясе устройства; null, если сервер не прислал. */
    val date: String?,
    val amount: Long,
)

/** Кнопка «Показать ещё» под списком. */
sealed interface LoadMoreState {
    /** Загружено всё. */
    data object Hidden : LoadMoreState
    data object Available : LoadMoreState
    data object Loading : LoadMoreState
    /** Следующая страница не загрузилась; уже загруженное остаётся на экране. */
    data class Error(val error: DataError) : LoadMoreState
}

sealed interface TransactionHistoryScreenState {
    data object Loading : TransactionHistoryScreenState
    data class Error(val error: DataError) : TransactionHistoryScreenState
    data class Content(val uiState: TransactionHistoryUiState) : TransactionHistoryScreenState
}
