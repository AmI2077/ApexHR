package com.example.apexhr.core.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountFormatterTest {

    @Test
    fun `small amounts are not grouped`() {
        assertEquals("0", formatAmount(0))
        assertEquals("110", formatAmount(110))
    }

    @Test
    fun `thousands are separated by non-breaking space`() {
        assertEquals("1\u00A0250", formatAmount(1_250))
        assertEquals("1\u00A0000\u00A0000", formatAmount(1_000_000))
    }

    @Test
    fun `negative amounts keep minus sign`() {
        assertEquals("-12\u00A0500", formatAmount(-12_500))
    }
}
