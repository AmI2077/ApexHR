package com.example.apexhr.features.activities

import com.example.apexhr.R
import com.example.apexhr.core.domain.AuthTokens
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.domain.map
import com.example.apexhr.core.ui.model.LoadMoreState
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.activities.data.AchievementsApi
import com.example.apexhr.features.activities.data.AchievementsRepositoryImpl
import com.example.apexhr.features.activities.data.dto.AchievementCatalogDto
import com.example.apexhr.features.activities.data.mergeAchievements
import com.example.apexhr.features.activities.domain.Achievement
import com.example.apexhr.features.activities.domain.AchievementCondition
import com.example.apexhr.features.activities.domain.AchievementsRepository
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.domain.ActivityPage
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventDetails
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.activities.domain.SurveyAnswer
import com.example.apexhr.features.activities.domain.SurveyDetails
import com.example.apexhr.features.activities.ui.ActivitiesViewModel
import com.example.apexhr.features.activities.ui.models.ActivitiesContentState
import com.example.apexhr.features.activities.ui.models.ActivitiesUiState
import com.example.apexhr.features.activities.ui.models.ActivityKind
import com.example.apexhr.features.activities.ui.models.ActivitySubtitle
import com.example.apexhr.features.activities.ui.models.toUiModel
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.profile.data.dto.AchievementDto
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesAchievementsTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun catalog(id: Long, condition: String? = "first_order", active: Boolean = true) =
        AchievementCatalogDto(
            id = id, name = "Ачивка $id", description = "Описание $id",
            rewardAmount = 10, conditionType = condition, isActive = active,
        )

    @Test
    fun `merge drops inactive achievements and marks earned ones by achievementId or nested id`() {
        val result = mergeAchievements(
            catalog = listOf(catalog(1), catalog(2, active = false), catalog(3, "first_thank"), catalog(4, "weird")),
            earned = listOf(
                UserAchievementDto(id = 10, achievementId = 1, earnedAt = "2026-11-10T09:00:00Z"),
                UserAchievementDto(id = 11, achievementId = 0, achievement = AchievementDto(id = 3)),
            ),
        )

        assertEquals(listOf(1L, 3L, 4L), result.map { it.id })
        assertTrue(result[0].isEarned)
        assertEquals(Instant.parse("2026-11-10T09:00:00Z"), result[0].earnedAt)
        assertTrue(result[1].isEarned)
        assertNull(result[1].earnedAt)
        assertFalse(result[2].isEarned)
        assertEquals(AchievementCondition.FirstThank, result[1].conditionType)
        assertEquals(AchievementCondition.Other, result[2].conditionType)
    }

    @Test
    fun `repository reads earned achievements of the user from the token`() = runTest {
        val profileApi = AchievementsFakeProfileApi()
        profileApi.earned["user-1"] = listOf(UserAchievementDto(id = 1, achievementId = 1))
        val repository = AchievementsRepositoryImpl(
            api = object : AchievementsApi {
                override suspend fun getAchievements() = listOf(catalog(1), catalog(2))
            },
            profileApi = profileApi,
            tokenStorage = AchievementsTokenStorage(jwt("user-1")),
        )

        val result = repository.getAchievements() as DataResult.Success

        assertEquals(listOf(true, false), result.data.map { it.isEarned })
        assertEquals(listOf("user-1"), profileApi.requestedUsers)
    }

    @Test
    fun `repository falls back to profile id when token has no subject`() = runTest {
        val profileApi = AchievementsFakeProfileApi()
        val repository = AchievementsRepositoryImpl(
            api = object : AchievementsApi {
                override suspend fun getAchievements() = listOf(catalog(1))
            },
            profileApi = profileApi,
            tokenStorage = AchievementsTokenStorage(null),
        )

        assertTrue(repository.getAchievements() is DataResult.Success)
        assertEquals(listOf("me"), profileApi.requestedUsers)
    }

    @Test
    fun `earned achievement shows date and reward, locked one shows condition, no participate button`() {
        val zone = ZoneOffset.ofHours(3)
        val earned = Achievement(
            id = 1, name = "Первый заказ", description = "Сделайте заказ", rewardAmount = 15,
            conditionType = AchievementCondition.FirstOrder, isEarned = true,
            earnedAt = Instant.parse("2026-11-10T09:00:00Z"),
        ).toUiModel(zone)
        val locked = Achievement(
            id = 2, name = "Спасибо", description = null, rewardAmount = 0,
            conditionType = AchievementCondition.FirstThank, isEarned = false, earnedAt = null,
        ).toUiModel(zone)

        assertEquals(ActivityKind.Achievement, earned.kind)
        assertFalse(earned.canParticipate)
        assertEquals(15L, earned.reward)
        assertEquals(R.string.activities_status_achievement_earned, earned.statusRes)
        val earnedInfo = earned.subtitle as ActivitySubtitle.AchievementInfo
        assertNull(earnedInfo.conditionRes)
        assertTrue(earnedInfo.earnedAt != null)

        assertNull(locked.reward)
        assertNull(locked.statusRes)
        val lockedInfo = locked.subtitle as ActivitySubtitle.AchievementInfo
        assertEquals(R.string.achievement_condition_first_thank, lockedInfo.conditionRes)
        assertNull(lockedInfo.earnedAt)
    }

    @Test
    fun `tabs start with All and end with Achievements like the web`() {
        val ids = ActivitiesUiState.Categories.map { it.id }
        assertEquals(ActivitiesUiState.CATEGORY_ALL, ids.first())
        assertEquals(ActivitiesUiState.CATEGORY_ACHIEVEMENTS, ids.last())
        assertEquals(ActivitiesUiState.CATEGORY_ALL, ActivitiesUiState.Initial.selectedCategoryId)
    }

    @Test
    fun `All tab skips a failed section and shows achievements`() {
        val activities = FakeActivitiesRepository().apply { events = DataResult.Failure(DataError.Network) }
        val viewModel = ActivitiesViewModel(activities, FakeAchievementsRepository())

        viewModel.onScreenShown()

        val content = viewModel.state.value.content as ActivitiesContentState.Content
        assertEquals(listOf(ActivityKind.Achievement), content.activities.map { it.kind })
    }

    @Test
    fun `All tab shows an error only when every section failed`() {
        val failure = DataResult.Failure(DataError.Network)
        val activities = FakeActivitiesRepository().apply { quests = failure; surveys = failure; events = failure }
        val viewModel = ActivitiesViewModel(activities, FakeAchievementsRepository(failure))

        viewModel.onScreenShown()

        assertEquals(ActivitiesContentState.Error(DataError.Network), viewModel.state.value.content)
    }

    @Test
    fun `Achievements tab loads only achievements`() {
        val activities = FakeActivitiesRepository()
        val viewModel = ActivitiesViewModel(activities, FakeAchievementsRepository())

        viewModel.onCategoryClick(ActivitiesUiState.CATEGORY_ACHIEVEMENTS)

        val content = viewModel.state.value.content as ActivitiesContentState.Content
        assertEquals(1, content.activities.size)
        assertEquals(0, activities.calls)
    }

    // Догрузка страниц

    @Test
    fun `surveys tab loads next page at list end and hides footer on last page`() {
        val activities = FakeActivitiesRepository().apply {
            surveyPages[1] = surveysPage(1, hasNext = true, 1, 2)
            surveyPages[2] = surveysPage(2, hasNext = false, 2, 3)
        }
        val viewModel = ActivitiesViewModel(activities, FakeAchievementsRepository())
        viewModel.onCategoryClick(ActivitiesUiState.CATEGORY_SURVEYS)
        assertEquals(LoadMoreState.Available, viewModel.content().loadMore)

        viewModel.onLoadMore()

        assertEquals(listOf(1L, 2L, 3L), viewModel.content().activities.map { it.id })
        assertEquals(LoadMoreState.Hidden, viewModel.content().loadMore)
        viewModel.onLoadMore()
        assertEquals(listOf(1, 2), activities.requestedSurveyPages)
    }

    @Test
    fun `load more error keeps list, shows retry and retry loads the page`() {
        val activities = FakeActivitiesRepository().apply {
            surveyPages[1] = surveysPage(1, hasNext = true, 1)
            surveyPages[2] = DataResult.Failure(DataError.Timeout)
        }
        val viewModel = ActivitiesViewModel(activities, FakeAchievementsRepository())
        viewModel.onCategoryClick(ActivitiesUiState.CATEGORY_SURVEYS)

        viewModel.onLoadMore()
        assertEquals(listOf(1L), viewModel.content().activities.map { it.id })
        assertEquals(LoadMoreState.Error(DataError.Timeout), viewModel.content().loadMore)

        activities.surveyPages[2] = surveysPage(2, hasNext = false, 2)
        viewModel.onLoadMore()
        assertEquals(listOf(1L, 2L), viewModel.content().activities.map { it.id })
        assertEquals(LoadMoreState.Hidden, viewModel.content().loadMore)
    }

    @Test
    fun `All tab continues only sections that have more pages and keeps section order`() {
        val activities = FakeActivitiesRepository().apply {
            questPages[1] = questsPage(1, hasNext = true, 10)
            questPages[2] = questsPage(2, hasNext = false, 11)
            surveyPages[1] = surveysPage(1, hasNext = false, 1)
        }
        val viewModel = ActivitiesViewModel(activities, FakeAchievementsRepository())
        viewModel.onScreenShown()

        viewModel.onLoadMore()

        val content = viewModel.content()
        assertEquals(
            listOf(ActivityKind.Quest to 10L, ActivityKind.Quest to 11L, ActivityKind.Survey to 1L, ActivityKind.Achievement to 1L),
            content.activities.map { it.kind to it.id },
        )
        assertEquals(listOf(1, 2), activities.requestedQuestPages)
        assertEquals(listOf(1), activities.requestedSurveyPages)
        assertEquals(LoadMoreState.Hidden, content.loadMore)
    }

    @Test
    fun `refresh starts again from the first page`() {
        val activities = FakeActivitiesRepository().apply {
            surveyPages[1] = surveysPage(1, hasNext = true, 1)
            surveyPages[2] = surveysPage(2, hasNext = false, 2)
        }
        val viewModel = ActivitiesViewModel(activities, FakeAchievementsRepository())
        viewModel.onCategoryClick(ActivitiesUiState.CATEGORY_SURVEYS)
        viewModel.onLoadMore()

        viewModel.onScreenShown()

        assertEquals(listOf(1L), viewModel.content().activities.map { it.id })
        assertEquals(LoadMoreState.Available, viewModel.content().loadMore)
        assertEquals(listOf(1, 2, 1), activities.requestedSurveyPages)
    }

    private fun ActivitiesViewModel.content() = state.value.content as ActivitiesContentState.Content

    private fun surveysPage(page: Int, hasNext: Boolean, vararg ids: Long) = DataResult.Success(
        ActivityPage(
            items = ids.map { Survey(it, "Опрос $it", null, questionsCount = 1, hasResponded = false, canRespond = true) },
            page = page,
            hasNextPage = hasNext,
        ),
    )

    private fun questsPage(page: Int, hasNext: Boolean, vararg ids: Long) = DataResult.Success(
        ActivityPage(
            items = ids.map { Quest(it, "Квест $it", null, rewardAmount = 5, stepsCount = 1, participationStatus = null) },
            page = page,
            hasNextPage = hasNext,
        ),
    )
}

/** JWT с нужным sub; подпись не проверяется. */
private fun jwt(sub: String): String {
    val encoder = java.util.Base64.getUrlEncoder().withoutPadding()
    val header = encoder.encodeToString("""{"alg":"none"}""".toByteArray())
    val payload = encoder.encodeToString("""{"sub":"$sub"}""".toByteArray())
    return "$header.$payload.sig"
}

private class AchievementsTokenStorage(private val token: String?) : TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens) = Unit
    override suspend fun getTokens(): AuthTokens? = null
    override suspend fun getAccessToken(): String? = token
    override suspend fun clearTokens() = Unit
}

private class AchievementsFakeProfileApi : ProfileApi {
    val earned = mutableMapOf<String, List<UserAchievementDto>>()
    val requestedUsers = mutableListOf<String>()

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> {
        requestedUsers += userId
        return earned[userId].orEmpty()
    }

    override suspend fun getCurrentUser(): CurrentUserProfileDto = CurrentUserProfileDto(id = "me")
    override suspend fun getBalance(): BalanceDto = throw UnsupportedOperationException()
    override suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto> =
        throw UnsupportedOperationException()
    override suspend fun updateOwnProfile(request: UpdateOwnProfileRequest) = throw UnsupportedOperationException()
}

private class FakeAchievementsRepository(
    private val result: DataResult<List<Achievement>> = DataResult.Success(
        listOf(
            Achievement(
                id = 1, name = "Первый заказ", description = null, rewardAmount = 10,
                conditionType = AchievementCondition.FirstOrder, isEarned = false, earnedAt = null,
            ),
        ),
    ),
) : AchievementsRepository {
    override suspend fun getAchievements() = result
}

private class FakeActivitiesRepository : ActivitiesRepository {
    var surveys: DataResult<List<Survey>> = DataResult.Success(emptyList())
    var quests: DataResult<List<Quest>> = DataResult.Success(emptyList())
    var events: DataResult<List<Event>> = DataResult.Success(emptyList())
    var calls = 0

    /** Постраничные ответы по номеру страницы; если заданы, важнее списков выше. */
    val surveyPages = mutableMapOf<Int, DataResult<ActivityPage<Survey>>>()
    val questPages = mutableMapOf<Int, DataResult<ActivityPage<Quest>>>()
    val requestedSurveyPages = mutableListOf<Int>()
    val requestedQuestPages = mutableListOf<Int>()

    override suspend fun getSurveys(page: Int): DataResult<ActivityPage<Survey>> {
        calls++
        requestedSurveyPages += page
        return surveyPages[page] ?: surveys.map { ActivityPage(it, page, hasNextPage = false) }
    }

    override suspend fun getQuests(page: Int): DataResult<ActivityPage<Quest>> {
        calls++
        requestedQuestPages += page
        return questPages[page] ?: quests.map { ActivityPage(it, page, hasNextPage = false) }
    }

    override suspend fun getEvents(page: Int) = events.also { calls++ }.map { ActivityPage(it, page, hasNextPage = false) }
    override suspend fun getSurveyDetails(surveyId: Long): DataResult<SurveyDetails> = throw UnsupportedOperationException()
    override suspend fun getQuestDetails(questId: Long): DataResult<QuestDetails> = throw UnsupportedOperationException()
    override suspend fun getEventDetails(eventId: Long): DataResult<EventDetails> = throw UnsupportedOperationException()
    override suspend fun submitSurvey(surveyId: Long, answers: Map<Long, SurveyAnswer>): ActivityActionResult =
        throw UnsupportedOperationException()
    override suspend fun joinQuest(questId: Long): ActivityActionResult = throw UnsupportedOperationException()
    override suspend fun completeQuestStep(questId: Long, stepId: Long): ActivityActionResult =
        throw UnsupportedOperationException()
    override suspend fun completeQuest(questId: Long): ActivityActionResult = throw UnsupportedOperationException()
    override suspend fun registerForEvent(eventId: Long): ActivityActionResult = throw UnsupportedOperationException()
    override suspend fun cancelEventRegistration(eventId: Long): ActivityActionResult =
        throw UnsupportedOperationException()
    override suspend fun claimEventReward(eventId: Long): ActivityActionResult = throw UnsupportedOperationException()
}