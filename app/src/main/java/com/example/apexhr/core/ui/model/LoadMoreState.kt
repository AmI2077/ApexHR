package com.example.apexhr.core.ui.model

import com.example.apexhr.core.domain.DataError

/** Низ постраничного списка: что показывать под уже загруженными элементами. */
sealed interface LoadMoreState {
    /** Загружено всё. */
    data object Hidden : LoadMoreState

    /** Есть ещё страницы: следующая загрузится, когда пользователь долистает до конца. */
    data object Available : LoadMoreState

    /** Следующая страница загружается. */
    data object Loading : LoadMoreState

    /** Следующая страница не загрузилась; уже загруженное остаётся на экране. */
    data class Error(val error: DataError) : LoadMoreState
}

/** Общее правило для всех списков: загрузка важнее ошибки, ошибка важнее «есть ещё». */
fun loadMoreStateOf(hasMore: Boolean, isLoading: Boolean, error: DataError?): LoadMoreState = when {
    isLoading -> LoadMoreState.Loading
    !hasMore -> LoadMoreState.Hidden
    error != null -> LoadMoreState.Error(error)
    else -> LoadMoreState.Available
}
