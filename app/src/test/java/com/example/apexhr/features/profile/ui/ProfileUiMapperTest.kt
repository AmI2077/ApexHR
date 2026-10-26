package com.example.apexhr.features.profile.ui

import com.example.apexhr.R
import com.example.apexhr.features.profile.domain.BalanceTransaction
import com.example.apexhr.features.profile.domain.EarnedAchievement
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.UserProfile
import com.example.apexhr.features.profile.ui.models.ProfileAchievementUiModel
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.TransactionUiModel
import com.example.apexhr.features.profile.ui.models.UserLabel
import com.example.apexhr.features.profile.ui.models.toUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileUiMapperTest {

    private val user = UserProfile(
        id = "user-1",
        firstName = "Анна",
        lastName = "Иванова",
        middleName = "Сергеевна",
        avatarUrl = "https://example.com/avatar.png",
        department = "Маркетинг",
        position = "Аналитик",
    )

    private val profile = Profile(
        user = user,
        balance = 1_250,
        transactions = listOf(
            BalanceTransaction(id = 7, amount = -100, description = "Заказ"),
            BalanceTransaction(id = 8, amount = 30, description = null),
        ),
        achievements = listOf(
            EarnedAchievement(id = 3, title = "Первая покупка", description = null, iconUrl = null),
        ),
    )

    @Test
    fun `profile is mapped to ui state`() {
        val state = profile.toUiState(ProfileTab.Achievements)

        assertEquals("Анна Иванова", state.name)
        assertEquals(
            listOf(UserLabel.Text("Аналитик"), UserLabel.Text("Маркетинг")),
            state.department,
        )
        assertEquals(1_250L, state.balance)
        assertEquals(ProfileTab.Achievements, state.selectedTab)
        assertEquals("https://example.com/avatar.png", state.avatarUrl)
        assertEquals(
            listOf(
                TransactionUiModel(id = "7", title = "Заказ", amount = -100),
                TransactionUiModel(id = "8", title = "", amount = 30),
            ),
            state.transactions,
        )
        assertEquals(listOf(ProfileAchievementUiModel(id = "3", title = "Первая покупка")), state.achievements)
    }

    @Test
    fun `loaded achievements are earned and keep condition description`() {
        val state = profile
            .copy(
                achievements = listOf(
                    EarnedAchievement(
                        id = 5,
                        title = "Первая благодарность",
                        description = "Для получения необходимо поблагодарить коллегу",
                        iconUrl = null,
                    ),
                ),
            )
            .toUiState(ProfileTab.Achievements)

        assertEquals(
            listOf(
                ProfileAchievementUiModel(
                    id = "5",
                    title = "Первая благодарность",
                    description = "Для получения необходимо поблагодарить коллегу",
                    isEarned = true,
                ),
            ),
            state.achievements,
        )
    }

    @Test
    fun `missing name and department parts are skipped`() {
        val state = profile
            .copy(user = user.copy(firstName = null, position = null))
            .toUiState(ProfileTab.Thanks)

        assertEquals("Иванова", state.name)
        assertEquals(listOf(UserLabel.Text("Маркетинг")), state.department)
    }

    @Test
    fun `server default position and department are translated`() {
        val state = profile
            .copy(user = user.copy(position = "Employee", department = "Unassigned"))
            .toUiState(ProfileTab.Thanks)

        assertEquals(
            listOf(
                UserLabel.Resource(R.string.profile_position_employee),
                UserLabel.Resource(R.string.profile_department_unassigned),
            ),
            state.department,
        )
    }

    @Test
    fun `avatar that is not a url is ignored`() {
        val state = profile
            .copy(user = user.copy(avatarUrl = "аватар"))
            .toUiState(ProfileTab.Thanks)

        assertNull(state.avatarUrl)
    }
}
