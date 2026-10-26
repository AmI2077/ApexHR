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

    @Test
    fun `min long is formatted without overflow`() {
        assertEquals("-9\u00A0223\u00A0372\u00A0036\u00A0854\u00A0775\u00A0808", formatAmount(Long.MIN_VALUE))
    }

    @Test
    fun `signed amount has plus for income`() {
        assertEquals("+150", formatSignedAmount(150))
        assertEquals("+1\u00A0250", formatSignedAmount(1_250))
    }

    @Test
    fun `signed amount has typographic minus for expense`() {
        assertEquals("\u221250", formatSignedAmount(-50))
        assertEquals("\u221212\u00A0500", formatSignedAmount(-12_500))
    }

    @Test
    fun `signed zero has no sign`() {
        assertEquals("0", formatSignedAmount(0))
    }

}