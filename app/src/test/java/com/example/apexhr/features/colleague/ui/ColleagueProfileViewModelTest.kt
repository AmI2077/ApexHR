package com.example.apexhr.features.colleague.ui

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.colleague.domain.ColleagueProfile
import com.example.apexhr.features.colleague.domain.ColleagueRepository
import com.example.apexhr.features.colleague.domain.ColleagueThank
import com.example.apexhr.features.colleague.domain.UserRole
import com.example.apexhr.features.colleague.ui.models.ColleagueProfileScreenState
import com.example.apexhr.features.colleague.ui.models.ColleagueProfileUiState
import com.example.apexhr.features.colleague.ui.models.toUiState
import com.example.apexhr.features.profile.domain.EarnedAchievement
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ColleagueProfileViewModelTest {

    private val repository = object : ColleagueRepository {
        val profiles = mutableMapOf<String, ColleagueProfile>()
        val requested = mutableListOf<String>()

        override suspend fun loadColleague(userId: String): DataResult<ColleagueProfile> {
            requested += userId
            return profiles[userId]?.let { DataResult.Success(it) } ?: DataResult.Failure(DataError.Network)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun ColleagueProfileViewModel.content(): ColleagueProfileUiState =
        (state.value as ColleagueProfileScreenState.Content).uiState

    @Test
    fun `colleague is shown and can be thanked`() {
        repository.profiles["u1"] = profile()

        val viewModel = ColleagueProfileViewModel("u1", repository)

        val content = viewModel.content()
        assertEquals("Анна Петрова", content.name)
        assertEquals(R.string.role_manager, content.roleRes)
        assertEquals(5L, content.thanksScore)
        assertTrue(content.canThank)
        assertEquals(listOf("Первая благодарность"), content.achievements.map { it.title })
    }

    @Test
    fun `blocked colleague and own profile cannot be thanked`() {
        repository.profiles["blocked"] = profile(isActive = false)
        repository.profiles["me"] = profile(isMe = true)

        assertFalse(ColleagueProfileViewModel("blocked", repository).content().canThank)
        assertFalse(ColleagueProfileViewModel("me", repository).content().canThank)
    }

    @Test
    fun `error and retry`() {
        val viewModel = ColleagueProfileViewModel("u1", repository)
        assertEquals(ColleagueProfileScreenState.Error(DataError.Network), viewModel.state.value)

        repository.profiles["u1"] = profile()
        viewModel.onRetryClick()

        assertEquals("Анна Петрова", viewModel.content().name)
        assertEquals(listOf("u1", "u1"), repository.requested)
    }

    @Test
    fun `thanks are formatted by date and avatar must be http`() {
        val state = profile(avatarUrl = "javascript:alert(1)").toUiState(ZoneOffset.UTC)

        assertNull(state.avatarUrl)
        assertEquals("05.11.2026", state.receivedThanks.single().date)
        assertTrue(state.receivedThanks.single().fromMe)
    }

    private fun profile(
        isActive: Boolean = true,
        isMe: Boolean = false,
        avatarUrl: String? = null,
    ) = ColleagueProfile(
        id = "u1",
        firstName = "Анна",
        lastName = "Петрова",
        avatarUrl = avatarUrl,
        position = "Маркетолог",
        department = "Маркетинг",
        role = UserRole.Manager,
        isActive = isActive,
        isMe = isMe,
        thanksScore = 5,
        receivedThanks = listOf(
            ColleagueThank(
                id = 1,
                fromMe = true,
                fromName = null,
                amount = 10,
                message = "Спасибо",
                createdAt = Instant.parse("2026-11-05T10:00:00Z"),
            )
        ),
        achievements = listOf(EarnedAchievement(id = 1, title = "Первая благодарность", description = null, iconUrl = null)),
    )
}