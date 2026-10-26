package com.example.apexhr.features.profile.data

import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.domain.HistoryTransaction
import com.example.apexhr.features.profile.domain.TransactionType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TransactionHistoryMappersTest {

    // Те же настройки, что в networkModule.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `transactions json is parsed into history`() {
        val transactions = json.decodeFromString<List<TransactionDto>>(
            """
            [{"id":11,"amount":-50,"balanceAfter":281,"description":"Заказ от 2026-10-25 20:16:44",
              "relatedUserId":null,"relatedEntityType":"order","relatedEntityId":5,
              "createdAt":"2026-10-25T20:16:44.773847Z"},
             {"id":10,"amount":1,"balanceAfter":331,"description":"ещё раз",
              "relatedUserId":"5b850be7-abe7-4a45-91a6-53dceeed040f","relatedEntityType":"thank",
              "relatedEntityId":7,"createdAt":"2026-10-25T18:55:04.265575Z"},
             {"id":1,"amount":100,"balanceAfter":100,"description":"Тестовое начисление валюты",
              "relatedUserId":null,"relatedEntityType":null,"relatedEntityId":null,
              "createdAt":"2026-10-19T21:33:41.89558Z"}]
            """.trimIndent()
        ).map { it.toHistoryDomain() }

        assertEquals(
            listOf(
                HistoryTransaction(
                    id = 11,
                    amount = -50,
                    description = "Заказ от 2026-10-25 20:16:44",
                    type = TransactionType.Purchase,
                    createdAt = Instant.parse("2026-10-25T20:16:44.773847Z"),
                ),
                HistoryTransaction(
                    id = 10,
                    amount = 1,
                    description = "ещё раз",
                    type = TransactionType.Thanks,
                    createdAt = Instant.parse("2026-10-25T18:55:04.265575Z"),
                ),
                HistoryTransaction(
                    id = 1,
                    amount = 100,
                    description = "Тестовое начисление валюты",
                    type = TransactionType.Adjustment,
                    createdAt = Instant.parse("2026-10-19T21:33:41.89558Z"),
                ),
            ),
            transactions,
        )
    }

    @Test
    fun `types seen on server are mapped`() {
        assertEquals(TransactionType.Purchase, transactionTypeOf("order", -100))
        assertEquals(TransactionType.Thanks, transactionTypeOf("thank", 50))
        assertEquals(TransactionType.Activity, transactionTypeOf("achievement", 10))
        assertEquals(TransactionType.Activity, transactionTypeOf("event", 100))
    }

    @Test
    fun `swagger enum names are mapped regardless of case and separators`() {
        assertEquals(TransactionType.Bonus, transactionTypeOf("ManagerBonus", 30))
        assertEquals(TransactionType.Bonus, transactionTypeOf("manager_bonus", 30))
        assertEquals(TransactionType.Activity, transactionTypeOf("ActivityReward", 10))
        assertEquals(TransactionType.Activity, transactionTypeOf("daily-bonus", 5))
        assertEquals(TransactionType.Activity, transactionTypeOf("AchievementReward", 10))
        assertEquals(TransactionType.Adjustment, transactionTypeOf("AdminAdjustment", -20))
        assertEquals(TransactionType.Refund, transactionTypeOf("Refund", 100))
        assertEquals(TransactionType.Thanks, transactionTypeOf("Thanks", 10))
        assertEquals(TransactionType.Purchase, transactionTypeOf("Purchase", -10))
    }

    @Test
    fun `positive order is a refund`() {
        assertEquals(TransactionType.Refund, transactionTypeOf("order", 100))
    }

    @Test
    fun `missing type is a manual adjustment`() {
        assertEquals(TransactionType.Adjustment, transactionTypeOf(null, 100))
        assertEquals(TransactionType.Adjustment, transactionTypeOf("  ", -5))
    }

    @Test
    fun `unknown type is other`() {
        assertEquals(TransactionType.Other, transactionTypeOf("lottery", 10))
        assertEquals(TransactionType.Other, transactionTypeOf("Other", 10))
    }

    @Test
    fun `type is not guessed from description`() {
        val transaction = TransactionDto(
            id = 9,
            amount = 10,
            description = "Получено достижение \"Первая благодарность\"",
            relatedEntityType = "achievement",
        ).toHistoryDomain()

        assertEquals(TransactionType.Activity, transaction.type)
    }

    @Test
    fun `date with offset is parsed`() {
        val transaction = TransactionDto(id = 1, createdAt = "2026-10-25T23:55:04+03:00").toHistoryDomain()

        assertEquals(Instant.parse("2026-10-25T20:55:04Z"), transaction.createdAt)
    }

    @Test
    fun `missing or broken date becomes null`() {
        assertNull(TransactionDto(id = 1, createdAt = null).toHistoryDomain().createdAt)
        assertNull(TransactionDto(id = 1, createdAt = "вчера").toHistoryDomain().createdAt)
    }

    @Test
    fun `blank description becomes null`() {
        assertNull(TransactionDto(id = 1, description = " ").toHistoryDomain().description)
    }
}
