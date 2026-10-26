package com.example.apexhr.features.home.data

import com.example.apexhr.features.home.data.dto.HomeUserDto
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

    private fun entry(rank: Int, userId: String?, name: String?, thanks: Long) =
        LeaderboardEntry(rank = rank, userId = userId, name = name, thanksCount = thanks)

    @Test
    fun `users json is parsed`() {
        val users = json.decodeFromString<List<HomeUserDto>>(
            """
            [{"id":"u-1","firstName":"Иван","lastName":"Петров","middleName":null,"avatarUrl":null,
              "department":"IT","position":"Dev","isActive":false,"lastLoginAt":null,
              "createdAt":"2026-10-01T00:00:00Z","updatedAt":"2026-10-01T00:00:00Z",
              "roles":["user"],"canAccessAdminPanel":false}]
            """.trimIndent()
        )

        assertEquals(listOf(HomeUserDto(id = "u-1", firstName = "Иван", lastName = "Петров", isActive = false)), users)
        assertEquals("Иван Петров", users.single().displayName())
    }

    @Test
    fun `active employees without thanks are added with zero after ranked`() {
        val merged = mergeLeaderboard(
            entries = listOf(entry(1, "u-1", "Иван Петров", 5), entry(2, "u-2", "Мария", 2)),
            users = listOf(
                HomeUserDto(id = "u-3", firstName = "Яна"),
                HomeUserDto(id = "u-1", firstName = "Иван", lastName = "Петров"),
                HomeUserDto(id = "u-4", firstName = "Анна", lastName = "Белова"),
                HomeUserDto(id = "u-2", firstName = "Мария"),
            ),
        )

        assertEquals(
            listOf(
                entry(1, "u-1", "Иван Петров", 5),
                entry(2, "u-2", "Мария", 2),
                entry(3, "u-4", "Анна Белова", 0),
                entry(4, "u-3", "Яна", 0),
            ),
            merged,
        )
    }

    @Test
    fun `no duplicates when ids differ in case or repeat`() {
        val merged = mergeLeaderboard(
            entries = listOf(entry(1, "ABC-1", "Иван", 3)),
            users = listOf(
                HomeUserDto(id = "abc-1", firstName = "Иван"),
                HomeUserDto(id = "u-2", firstName = "Пётр"),
                HomeUserDto(id = "U-2", firstName = "Пётр"),
            ),
        )

        assertEquals(listOf("ABC-1", "u-2"), merged.map { it.userId })
    }

    @Test
    fun `inactive and id-less employees are not added`() {
        val merged = mergeLeaderboard(
            entries = emptyList(),
            users = listOf(
                HomeUserDto(id = "u-1", firstName = "Уволен", isActive = false),
                HomeUserDto(id = null, firstName = "Без id"),
                HomeUserDto(id = "  ", firstName = "Пустой id"),
                HomeUserDto(id = "u-2", firstName = "Пётр"),
            ),
        )

        assertEquals(listOf(entry(1, "u-2", "Пётр", 0)), merged)
    }

    @Test
    fun `merged list is sorted by thanks and ranks are renumbered`() {
        val merged = mergeLeaderboard(
            // Сервер мог прислать и нулевые строки, и места не по порядку.
            entries = listOf(entry(3, "c", "C", 0), entry(2, "b", "B", 4), entry(1, "a", "A", 7)),
            users = listOf(HomeUserDto(id = "d", firstName = "D")),
        )

        assertEquals(listOf("a", "b", "c", "d"), merged.map { it.userId })
        assertEquals(listOf(1, 2, 3, 4), merged.map { it.rank })
        assertEquals(listOf(7L, 4L, 0L, 0L), merged.map { it.thanksCount })
    }

    @Test
    fun `missing leaderboard name is taken from employees`() {
        val merged = mergeLeaderboard(
            entries = listOf(entry(1, "u-1", null, 2)),
            users = listOf(HomeUserDto(id = "u-1", firstName = "Иван", lastName = "Петров")),
        )

        assertEquals("Иван Петров", merged.single().name)
    }
}