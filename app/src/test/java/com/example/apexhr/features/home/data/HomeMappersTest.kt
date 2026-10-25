package com.example.apexhr.features.home.data

import com.example.apexhr.features.home.data.dto.LeaderboardEntryDto
import com.example.apexhr.features.home.data.dto.QuestListItemDto
import com.example.apexhr.features.home.data.dto.QuestPageDto
import com.example.apexhr.features.home.domain.LeaderboardEntry
import com.example.apexhr.features.home.domain.Quest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeMappersTest {

    // Те же настройки, что в networkModule.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `quests page json is parsed and mapped`() {
        val page = json.decodeFromString<QuestPageDto>(
            """
            {"items":[{"id":1,"title":"Тестовый квест","description":"Описание","rewardAmount":100,
              "startAt":"2026-10-23T19:38:22.939Z","endAt":"2026-10-31T19:38:22.939Z",
              "stepsCount":2,"requiredStepsCount":2,"participationStatus":null}],
             "totalCount":1,"page":1,"pageSize":20,"totalPages":1,
             "hasPreviousPage":false,"hasNextPage":false}
            """.trimIndent()
        )

        assertEquals(
            listOf(Quest(id = 1, title = "Тестовый квест", rewardAmount = 100)),
            page.items.map { it.toDomain() },
        )
    }

    @Test
    fun `leaderboard json is parsed and mapped`() {
        val entries = json.decodeFromString<List<LeaderboardEntryDto>>(
            """
            [{"rank":1,"userId":"u-1","name":"Иванов Иван","avatar":"test","thanksCount":12},
             {"rank":2,"userId":"u-2","name":"  ","avatar":null,"thanksCount":0}]
            """.trimIndent()
        )

        assertEquals(
            listOf(
                LeaderboardEntry(rank = 1, userId = "u-1", name = "Иванов Иван", thanksCount = 12),
                LeaderboardEntry(rank = 2, userId = "u-2", name = null, thanksCount = 0),
            ),
            entries.map { it.toDomain() },
        )
    }

    @Test
    fun `quest title is trimmed and blank becomes null`() {
        assertEquals("Квест", QuestListItemDto(id = 1, title = " Квест ").toDomain().title)
        assertNull(QuestListItemDto(id = 1, title = "").toDomain().title)
    }

    @Test
    fun `quest is active unless completed or cancelled`() {
        assertTrue(QuestListItemDto(id = 1, participationStatus = null).isActiveForUser())
        assertTrue(QuestListItemDto(id = 1, participationStatus = "in_progress").isActiveForUser())
        assertFalse(QuestListItemDto(id = 1, participationStatus = "completed").isActiveForUser())
        assertFalse(QuestListItemDto(id = 1, participationStatus = "cancelled").isActiveForUser())
    }
}
