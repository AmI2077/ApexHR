package com.example.apexhr.features.home.ui

import com.example.apexhr.features.home.domain.HomeData
import com.example.apexhr.features.home.domain.LeaderboardEntry
import com.example.apexhr.features.home.ui.models.LeaderboardEntryUiModel
import com.example.apexhr.features.home.ui.models.toUiState
import com.example.apexhr.features.profile.domain.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Строка «Ваше место» под лидербордом, как в вебе. */
class HomeMyPlaceTest {

    private fun home(myIndex: Int?, size: Int = 8) = HomeData(
        user = UserProfile("ME", "Дмитрий", "Новохатский", null, null, null, null),
        balance = 0,
        quests = emptyList(),
        leaderboard = (0 until size).map { index ->
            LeaderboardEntry(
                rank = index + 1,
                userId = if (index == myIndex) "me" else "u-$index",
                name = "Сотрудник $index",
                thanksCount = (size - index).toLong(),
            )
        },
        achievements = emptyList(),
    )

    @Test
    fun `no extra row when I am in top five`() {
        val state = home(myIndex = 4).toUiState()

        assertNull(state.myLeaderboardEntry)
        assertTrue(state.leaderboard[4].isMine)
        assertEquals(1, state.leaderboard.count { it.isMine })
    }

    @Test
    fun `my place is shown below when I am outside top`() {
        val state = home(myIndex = 6).toUiState()

        assertEquals(
            LeaderboardEntryUiModel(id = "ME", rank = 7, name = "Сотрудник 6", thanksCount = "2", isMine = true),
            state.myLeaderboardEntry,
        )
    }

    @Test
    fun `missing from rating means place after the last`() {
        val state = home(myIndex = null, size = 3).toUiState()

        assertEquals(
            LeaderboardEntryUiModel(id = "ME", rank = 4, name = "Дмитрий Новохатский", thanksCount = "0", isMine = true),
            state.myLeaderboardEntry,
        )
    }
}