package com.example.apexhr.features.profile.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionFilterTest {

    private fun TransactionFilter.matching() = TransactionType.entries.filter { matches(it) }

    @Test
    fun `all shows every type`() {
        assertEquals(TransactionType.entries, TransactionFilter.All.matching())
    }

    @Test
    fun `each filter shows its types`() {
        assertEquals(listOf(TransactionType.Activity), TransactionFilter.Activities.matching())
        assertEquals(listOf(TransactionType.Thanks), TransactionFilter.Thanks.matching())
        assertEquals(listOf(TransactionType.Bonus), TransactionFilter.Bonuses.matching())
        assertEquals(listOf(TransactionType.Adjustment), TransactionFilter.Adjustments.matching())
    }

    @Test
    fun `refunds have their own filter like on the web`() {
        assertEquals(listOf(TransactionType.Purchase), TransactionFilter.Purchases.matching())
        assertEquals(listOf(TransactionType.Refund), TransactionFilter.Refunds.matching())
    }

    @Test
    fun `filters go in web order`() {
        assertEquals(
            listOf(
                TransactionFilter.All, TransactionFilter.Activities, TransactionFilter.Thanks,
                TransactionFilter.Bonuses, TransactionFilter.Purchases, TransactionFilter.Refunds,
                TransactionFilter.Adjustments,
            ),
            TransactionFilter.entries,
        )
    }

    @Test
    fun `other is shown only in all`() {
        assertEquals(
            listOf(TransactionFilter.All),
            TransactionFilter.entries.filter { it.matches(TransactionType.Other) },
        )
    }
}
