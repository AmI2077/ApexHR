package com.example.apexhr.features.profile.data

import com.example.apexhr.features.profile.data.dto.AchievementDto
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import com.example.apexhr.features.profile.domain.BalanceTransaction
import com.example.apexhr.features.profile.domain.EarnedAchievement
import com.example.apexhr.features.profile.domain.UserProfile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileMappersTest {

    // Те же настройки, что в networkModule.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `user profile json is parsed and mapped`() {
        val dto = json.decodeFromString<CurrentUserProfileDto>(
            """
            {"id":"11111111-2222-3333-4444-555555555555","firstName":"Анна","lastName":"Иванова",
             "middleName":null,"avatarUrl":null,"department":"Маркетинг","position":"Аналитик",
             "isActive":true,"roles":["employee"],"canAccessAdminPanel":false}
            """.trimIndent()
        )

        assertEquals(
            UserProfile(
                id = "11111111-2222-3333-4444-555555555555",
                firstName = "Анна",
                lastName = "Иванова",
                middleName = null,
                avatarUrl = null,
                department = "Маркетинг",
                position = "Аналитик",
            ),
            dto.toDomain(),
        )
    }

    @Test
    fun `blank strings become null`() {
        val user = CurrentUserProfileDto(id = "id", firstName = "  ", department = "").toDomain()

        assertNull(user.firstName)
        assertNull(user.department)
    }

    @Test
    fun `balance and transactions json are parsed`() {
        val balance = json.decodeFromString<BalanceDto>(
            """{"userId":"id","type":"Personal","amount":110,"version":6,"updatedAt":"2026-10-21T22:10:02Z"}"""
        )
        val transactions = json.decodeFromString<List<TransactionDto>>(
            """
            [{"id":10,"amount":10,"balanceAfter":110,"description":"Получено достижение",
              "relatedUserId":null,"relatedEntityType":"achievement","relatedEntityId":1,
              "createdAt":"2026-10-21T22:10:02Z"},
             {"id":9,"amount":-100,"balanceAfter":100,"description":"Заказ",
              "relatedEntityType":"order","relatedEntityId":4,"createdAt":"2026-10-21T22:10:00Z"}]
            """.trimIndent()
        )

        assertEquals(110L, balance.amount)
        assertEquals(
            listOf(
                BalanceTransaction(id = 10, amount = 10, description = "Получено достижение"),
                BalanceTransaction(id = 9, amount = -100, description = "Заказ"),
            ),
            transactions.map { it.toDomain() },
        )
    }

    @Test
    fun `user achievement takes name from nested achievement`() {
        val dto = json.decodeFromString<List<UserAchievementDto>>(
            """
            [{"id":1,"userId":"id","achievementId":1,"earnedAt":"2026-10-21T22:10:02Z",
              "achievement":{"id":1,"code":"FIRST_ORDER","name":"Первая покупка",
                "description":"Совершить первую покупку","iconUrl":null,"rewardAmount":10,
                "conditionType":"first_order","conditionValue":1,"isActive":true}}]
            """.trimIndent()
        ).single()

        assertEquals(
            EarnedAchievement(
                id = 1,
                title = "Первая покупка",
                description = "Совершить первую покупку",
                iconUrl = null,
            ),
            dto.toDomain(),
        )
    }

    @Test
    fun `user achievement without nested achievement has no title`() {
        val achievement = UserAchievementDto(id = 5, achievement = null).toDomain()

        assertNull(achievement.title)
        assertEquals(5L, achievement.id)
    }

    @Test
    fun `nested achievement name is trimmed`() {
        val achievement = UserAchievementDto(
            id = 1,
            achievement = AchievementDto(name = "  Активность  "),
        ).toDomain()

        assertEquals("Активность", achievement.title)
    }
}
