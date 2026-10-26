package com.example.apexhr.features.profile.ui.history

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.profile.domain.HistoryTransaction
import com.example.apexhr.features.profile.domain.TransactionFilter
import com.example.apexhr.features.profile.domain.TransactionPage
import com.example.apexhr.features.profile.domain.TransactionType
import com.example.apexhr.features.profile.ui.history.models.HistoryTransactionUiModel
import com.example.apexhr.features.profile.ui.history.models.LoadMoreState
import com.example.apexhr.features.profile.ui.history.models.LoadedTransactions
import com.example.apexhr.features.profile.ui.history.models.labelRes
import com.example.apexhr.features.profile.ui.history.models.toUiModel
import com.example.apexhr.features.profile.ui.history.models.toUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class TransactionHistoryUiMapperTest {

    private fun transaction(
        id: Long,
        type: TransactionType = TransactionType.Activity,
        amount: Long = 10,
    ) = HistoryTransaction(
        id = id,
        amount = amount,
        description = "Операция $id",
        type = type,
        createdAt = null,
    )

    private fun page(vararg ids: Long, hasMore: Boolean = true) =
        TransactionPage(transactions = ids.map { transaction(it) }, hasMore = hasMore)

    // Пагинация

    @Test
    fun `empty history waits for the first page`() {
        assertEquals(0, LoadedTransactions.Empty.nextSkip)
        assertTrue(LoadedTransactions.Empty.hasMore)
        assertTrue(LoadedTransactions.Empty.items.isEmpty())
    }

    @Test
    fun `pages are appended in order and skip grows`() {
        val loaded = LoadedTransactions.Empty + page(3, 2) + page(1, hasMore = false)

        assertEquals(listOf(3L, 2L, 1L), loaded.items.map { it.id })
        assertEquals(3, loaded.nextSkip)
        assertFalse(loaded.hasMore)
    }

    @Test
    fun `repeated operations after shift are dropped but counted in skip`() {
        // Между запросами появилась новая операция: id 2 пришёл повторно.
        val loaded = LoadedTransactions.Empty + page(3, 2) + page(2, 1)

        assertEquals(listOf(3L, 2L, 1L), loaded.items.map { it.id })
        assertEquals(4, loaded.nextSkip)
    }

    @Test
    fun `empty page ends history`() {
        val loaded = LoadedTransactions.Empty + page(2, 1) + page(hasMore = false)

        assertEquals(2, loaded.nextSkip)
        assertFalse(loaded.hasMore)
    }

    // Фильтр

    @Test
    fun `filter keeps only matching loaded operations`() {
        val loaded = LoadedTransactions.Empty + TransactionPage(
            transactions = listOf(
                transaction(4, TransactionType.Purchase, amount = -50),
                transaction(3, TransactionType.Thanks),
                transaction(2, TransactionType.Refund, amount = 50),
                transaction(1, TransactionType.Activity),
            ),
            hasMore = false,
        )

        assertEquals(
            listOf("4", "2"),
            loaded.toUiState(TransactionFilter.Purchases).transactions.map { it.id },
        )
        assertEquals(
            listOf("3"),
            loaded.toUiState(TransactionFilter.Thanks).transactions.map { it.id },
        )
        assertEquals(4, loaded.toUiState(TransactionFilter.All).transactions.size)
    }

    @Test
    fun `selected filter is kept in state`() {
        val state = LoadedTransactions.Empty.toUiState(TransactionFilter.Bonuses)

        assertEquals(TransactionFilter.Bonuses, state.selectedFilter)
    }

    @Test
    fun `empty text depends on what is loaded`() {
        val nothing = LoadedTransactions.Empty + page(hasMore = false)
        val partial = LoadedTransactions.Empty + page(1)
        val all = LoadedTransactions.Empty + page(1, hasMore = false)

        assertEquals(R.string.profile_history_empty, nothing.toUiState(TransactionFilter.All).emptyTextRes)
        assertEquals(
            R.string.profile_history_filter_empty_loaded,
            partial.toUiState(TransactionFilter.Bonuses).emptyTextRes,
        )
        assertEquals(
            R.string.profile_history_filter_empty,
            all.toUiState(TransactionFilter.Bonuses).emptyTextRes,
        )
    }

    // Кнопка «Показать ещё»

    @Test
    fun `load more is available while server has more`() {
        val loaded = LoadedTransactions.Empty + page(1)

        assertEquals(LoadMoreState.Available, loaded.toUiState(TransactionFilter.All).loadMore)
    }

    @Test
    fun `load more is hidden after the last page`() {
        val loaded = LoadedTransactions.Empty + page(1, hasMore = false)

        assertEquals(LoadMoreState.Hidden, loaded.toUiState(TransactionFilter.All).loadMore)
    }

    @Test
    fun `load more shows progress and error`() {
        val loaded = LoadedTransactions.Empty + page(1)

        assertEquals(
            LoadMoreState.Loading,
            loaded.toUiState(TransactionFilter.All, isLoadingMore = true).loadMore,
        )
        assertEquals(
            LoadMoreState.Error(DataError.Network),
            loaded.toUiState(TransactionFilter.All, loadMoreError = DataError.Network).loadMore,
        )
    }

    @Test
    fun `load more error keeps loaded operations`() {
        val loaded = LoadedTransactions.Empty + page(2, 1)

        val state = loaded.toUiState(TransactionFilter.All, loadMoreError = DataError.Server)

        assertEquals(listOf("2", "1"), state.transactions.map { it.id })
    }

    // Операция

    @Test
    fun `transaction is mapped with type and local date`() {
        val transaction = HistoryTransaction(
            id = 11,
            amount = -50,
            description = "Заказ от 2026-10-25 20:16:44",
            type = TransactionType.Purchase,
            createdAt = Instant.parse("2026-10-25T20:16:44.773847Z"),
        )

        assertEquals(
            HistoryTransactionUiModel(
                id = "11",
                title = "Заказ от 2026-10-25 20:16:44",
                typeRes = R.string.profile_history_type_purchase,
                date = "25.10.2026 23:16",
                amount = -50,
            ),
            transaction.toUiModel(zone = ZoneId.of("Europe/Moscow")),
        )
    }

    @Test
    fun `missing description and date stay empty`() {
        val ui = transaction(1).copy(description = null).toUiModel(zone = ZoneOffset.UTC)

        assertEquals("", ui.title)
        assertNull(ui.date)
    }

    @Test
    fun `every filter and type has a label`() {
        assertEquals(
            TransactionFilter.entries.size,
            TransactionFilter.entries.map { it.labelRes() }.toSet().size,
        )
        assertEquals(
            TransactionType.entries.size,
            TransactionType.entries.map { it.labelRes() }.toSet().size,
        )
    }
}
