package com.example.apexhr.features.home.ui

import com.example.apexhr.features.home.domain.HomeAchievement
import com.example.apexhr.features.home.domain.HomeData
import com.example.apexhr.features.home.domain.LeaderboardEntry
import com.example.apexhr.features.home.domain.Quest
import com.example.apexhr.features.home.ui.models.AchievementUiModel
import com.example.apexhr.features.home.ui.models.ChallengeUiModel
import com.example.apexhr.features.home.ui.models.LeaderboardEntryUiModel
import com.example.apexhr.features.home.ui.models.toUiState
import com.example.apexhr.features.profile.domain.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeUiMapperTest {

    private val data = HomeData(
        user = UserProfile(
            id = "user-1",
            firstName = "Анна",
            lastName = "Иванова",
            middleName = null,
            avatarUrl = null,
            department = null,
            position = null,
        ),
        balance = 1_250,
        quests = listOf(Quest(id = 7, title = "7 дней активности", rewardAmount = 300)),
        leaderboard = listOf(
            LeaderboardEntry(rank = 1, userId = "u-1", name = "Иван П.", thanksCount = 12_456),
            LeaderboardEntry(rank = 2, userId = null, name = null, thanksCount = 3),
        ),
        achievements = listOf(
            HomeAchievement(id = 4, title = "Первое мероприятие", description = "Посетите мероприятие", isEarned = true),
            HomeAchievement(id = 5, title = null, description = "Завершите квест", isEarned = false),
        ),
    )

    @Test
    fun `home data is mapped to ui state`() {
        val state = data.toUiState()

        assertEquals("Анна", state.userName)
        assertEquals(1_250L, state.balance)
        assertEquals(listOf(ChallengeUiModel(id = "7", title = "7 дней активности", prizeAmount = 300)), state.challenges)
    }

    @Test
    fun `achievements keep earned flag and description`() {
        assertEquals(
            listOf(
                AchievementUiModel(
                    id = "4",
                    title = "Первое мероприятие",
                    description = "Посетите мероприятие",
                    isEarned = true,
                ),
                AchievementUiModel(id = "5", title = "", description = "Завершите квест", isEarned = false),
            ),
            data.toUiState().achievements,
        )
    }

    @Test
    fun `leaderboard shows thanks count and falls back to rank key`() {
        assertEquals(
            listOf(
                LeaderboardEntryUiModel(id = "u-1", rank = 1, name = "Иван П.", thanksCount = "12\u00A0456"),
                LeaderboardEntryUiModel(id = "rank_2_1", rank = 2, name = "", thanksCount = "3"),
            ),
            data.toUiState().leaderboard,
        )
    }

    @Test
    fun `missing first name gives empty user name`() {
        val state = data.copy(user = data.user.copy(firstName = null)).toUiState()

        assertEquals("", state.userName)
    }
}
