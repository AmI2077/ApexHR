package com.example.apexhr.features.profile.ui

import com.example.apexhr.R
import com.example.apexhr.features.profile.domain.EarnedAchievement
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.UserProfile
import com.example.apexhr.features.profile.ui.history.models.TransactionHistoryState
import com.example.apexhr.features.profile.ui.models.ProfileAchievementUiModel
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.ThankUiModel
import com.example.apexhr.features.profile.ui.models.UserLabel
import com.example.apexhr.features.profile.ui.models.toUiState
import com.example.apexhr.features.thanks.domain.Thank
import com.example.apexhr.features.thanks.domain.ThanksHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

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
        achievements = listOf(
            EarnedAchievement(id = 3, title = "Первая покупка", description = null, iconUrl = null),
        ),
    )

    private val thanks = ThanksHistory(
        received = listOf(
            Thank(
                id = 5,
                counterpartId = "ivan",
                counterpartName = "Иван Петров",
                amount = 20,
                message = "Спасибо",
                createdAt = Instant.parse("2026-10-25T22:30:00Z"),
            ),
        ),
        sent = listOf(
            Thank(id = 6, counterpartId = "gone", counterpartName = null, amount = 10, message = null, createdAt = null),
        ),
    )

    private fun Profile.toUiState(tab: ProfileTab) = toUiState(tab, thanks, zone = ZoneOffset.ofHours(3))

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
        assertEquals(listOf(ProfileAchievementUiModel(id = "3", title = "Первая покупка")), state.achievements)
    }

    @Test
    fun `thanks are mapped with local date and unknown name`() {
        val state = profile.toUiState(ProfileTab.Thanks)

        assertEquals(
            listOf(ThankUiModel(id = "5", name = "Иван Петров", amount = 20, message = "Спасибо", date = "26.10.2026")),
            state.received,
        )
        assertEquals(
            listOf(ThankUiModel(id = "6", name = null, amount = 10, message = null, date = null)),
            state.sent,
        )
    }

    @Test
    fun `no thanks give empty lists`() {
        val state = profile.toUiState(ProfileTab.Thanks, ThanksHistory(received = emptyList(), sent = emptyList()))

        assertEquals(emptyList<ThankUiModel>(), state.received)
        assertEquals(emptyList<ThankUiModel>(), state.sent)
    }

    @Test
    fun `tabs go in order thanks, achievements, operations`() {
        assertEquals(
            listOf(ProfileTab.Thanks, ProfileTab.Achievements, ProfileTab.Operations),
            ProfileTab.entries.toList(),
        )
    }

    @Test
    fun `operations tab starts in loading state`() {
        assertEquals(TransactionHistoryState.Loading, profile.toUiState(ProfileTab.Operations).operations)
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
                UserLabel.Resource(R.string.role_employee),
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
