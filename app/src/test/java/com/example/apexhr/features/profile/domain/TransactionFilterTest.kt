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
    fun `purchases include refunds`() {
        assertEquals(
            listOf(TransactionType.Purchase, TransactionType.Refund),
            TransactionFilter.Purchases.matching(),
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
