package com.example.apexhr.features.profile.ui.history.models

import com.example.apexhr.features.profile.domain.HistoryTransaction
import com.example.apexhr.features.profile.domain.TransactionPage

/**
 * Загруженная часть истории.
 * @param nextSkip сколько операций уже отдал сервер — skip следующей страницы.
 * @param hasMore можно ли загрузить ещё.
 */
data class LoadedTransactions(
    val items: List<HistoryTransaction>,
    val nextSkip: Int,
    val hasMore: Boolean,
) {
    /**
     * Добавляет следующую страницу. Если между запросами появилась новая
     * операция, список на сервере сдвигается и операция с конца прошлой
     * страницы приходит снова — такие повторы отбрасываем по id.
     */
    operator fun plus(page: TransactionPage): LoadedTransactions {
        val knownIds = items.mapTo(HashSet()) { it.id }
        return LoadedTransactions(
            items = items + page.transactions.filter { knownIds.add(it.id) },
            nextSkip = nextSkip + page.transactions.size,
            hasMore = page.hasMore,
        )
    }

    companion object {
        val Empty = LoadedTransactions(items = emptyList(), nextSkip = 0, hasMore = true)
    }
}
