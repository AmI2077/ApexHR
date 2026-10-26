package com.example.apexhr.core.ui.model

import com.example.apexhr.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ApexWordFormsTest {

    private fun genitive(amount: Long) = apexAmountRes(amount, ApexCase.Genitive)
    private fun nominative(amount: Long) = apexAmountRes(amount, ApexCase.Nominative)

    // --- Форма слова ---

    @Test
    fun `one for numbers ending in 1 except 11`() {
        listOf(1L, 21, 101, 1_001, 1_000_021).forEach {
            assertEquals("amount=$it", RussianPluralForm.One, russianPluralForm(it))
        }
    }

    @Test
    fun `few for numbers ending in 2-4 except 12-14`() {
        listOf(2L, 3, 4, 22, 34, 102, 1_254).forEach {
            assertEquals("amount=$it", RussianPluralForm.Few, russianPluralForm(it))
        }
    }

    @Test
    fun `many for zero, 5-20 and teens of hundreds`() {
        listOf(0L, 5, 9, 10, 11, 12, 14, 19, 20, 25, 100, 111, 112, 114, 1_250, 1_000_000).forEach {
            assertEquals("amount=$it", RussianPluralForm.Many, russianPluralForm(it))
        }
    }

    @Test
    fun `form ignores sign`() {
        assertEquals(RussianPluralForm.One, russianPluralForm(-1))
        assertEquals(RussianPluralForm.One, russianPluralForm(-21))
        assertEquals(RussianPluralForm.Few, russianPluralForm(-2))
        assertEquals(RussianPluralForm.Many, russianPluralForm(-5))
        assertEquals(RussianPluralForm.Many, russianPluralForm(-11))
        assertEquals(RussianPluralForm.Many, russianPluralForm(-111))
    }

    @Test
    fun `extreme longs do not overflow`() {
        // ...807 и ...808: последние две цифры 07 и 08.
        assertEquals(RussianPluralForm.Many, russianPluralForm(Long.MAX_VALUE))
        assertEquals(RussianPluralForm.Many, russianPluralForm(Long.MIN_VALUE))
    }

    // --- Родительный падеж: «Не хватает N …» ---

    @Test
    fun `genitive 1 21 101 is apeksa`() {
        assertEquals(R.string.apex_amount_genitive_one, genitive(1))
        assertEquals(R.string.apex_amount_genitive_one, genitive(21))
        assertEquals(R.string.apex_amount_genitive_one, genitive(101))
    }

    @Test
    fun `genitive 2 5 11 111 1250 is apeksov`() {
        assertEquals(R.string.apex_amount_genitive_many, genitive(2))
        assertEquals(R.string.apex_amount_genitive_many, genitive(5))
        assertEquals(R.string.apex_amount_genitive_many, genitive(11))
        assertEquals(R.string.apex_amount_genitive_many, genitive(111))
        assertEquals(R.string.apex_amount_genitive_many, genitive(1_250))
    }

    @Test
    fun `genitive of negative amounts uses absolute value`() {
        assertEquals(R.string.apex_amount_genitive_one, genitive(-1))
        assertEquals(R.string.apex_amount_genitive_one, genitive(-21))
        assertEquals(R.string.apex_amount_genitive_many, genitive(-2))
        assertEquals(R.string.apex_amount_genitive_many, genitive(-5))
        assertEquals(R.string.apex_amount_genitive_many, genitive(-11))
    }

    @Test
    fun `genitive word without number`() {
        assertEquals(R.string.apex_word_genitive_one, apexWordRes(21, ApexCase.Genitive))
        assertEquals(R.string.apex_word_genitive_many, apexWordRes(3, ApexCase.Genitive))
        assertEquals(R.string.apex_word_genitive_many, apexWordRes(111, ApexCase.Genitive))
    }

    // --- Именительный падеж (поведение как раньше) ---

    @Test
    fun `nominative forms`() {
        assertEquals(R.string.apex_amount_one, nominative(1))
        assertEquals(R.string.apex_amount_one, nominative(21))
        assertEquals(R.string.apex_amount_one, nominative(101))
        assertEquals(R.string.apex_amount_few, nominative(2))
        assertEquals(R.string.apex_amount_few, nominative(-3))
        assertEquals(R.string.apex_amount_many, nominative(5))
        assertEquals(R.string.apex_amount_many, nominative(11))
        assertEquals(R.string.apex_amount_many, nominative(111))
        assertEquals(R.string.apex_amount_many, nominative(1_250))
    }

    @Test
    fun `nominative is default case`() {
        assertEquals(R.string.apex_amount_few, apexAmountRes(4))
        assertEquals(R.string.apex_word_one, apexWordRes(1))
        assertEquals(R.string.apex_word_many, apexWordRes(0))
    }
}