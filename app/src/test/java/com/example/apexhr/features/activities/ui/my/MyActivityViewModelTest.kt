package com.example.apexhr.features.activities.ui.my

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.domain.Achievement
import com.example.apexhr.features.activities.domain.AchievementCondition
import com.example.apexhr.features.activities.domain.AchievementsRepository
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.domain.ActivityPage
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventDetails
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.activities.domain.SurveyAnswer
import com.example.apexhr.features.activities.domain.SurveyDetails
import com.example.apexhr.features.activities.ui.my.models.MyActivityScreenState
import com.example.apexhr.features.engagement.domain.ActivityLevel
import com.example.apexhr.features.engagement.domain.EngagementRepository
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
class MyActivityViewModelTest {

    private val activities = MyActivityFakeRepository()
    private val achievements = MyActivityFakeAchievements()
    private val engagement = MyActivityFakeEngagement()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun MyActivityViewModel.content() = (state.value as MyActivityScreenState.Content).uiState

    @Test
    fun `shows only what the user takes part in, like the web`() {
        activities.quests = DataResult.Success(
            listOf(
                quest(1, QuestParticipationStatus.InProgress),
                quest(2, QuestParticipationStatus.Completed),
                quest(3, null),
                quest(4, QuestParticipationStatus.Cancelled),
            ),
        )
        activities.surveys = DataResult.Success(listOf(survey(1, answered = true, reward = 50), survey(2, answered = false, reward = 5)))
        activities.events = DataResult.Success(
            listOf(
                event(1, EventRegistrationStatus.Registered),
                event(2, EventRegistrationStatus.Attended),
                event(3, EventRegistrationStatus.Cancelled),
                event(4, null),
            ),
        )
        achievements.result = DataResult.Success(listOf(achievement(1, earned = true), achievement(2, earned = false)))
        val viewModel = MyActivityViewModel(activities, achievements, engagement)

        viewModel.onScreenShown()

        val ui = viewModel.content()
        assertEquals(
            listOf(1L to R.string.my_activity_quest_in_progress, 2L to R.string.my_activity_quest_completed),
            ui.quests.map { it.id to it.statusRes },
        )
        assertEquals(listOf(1L to 50L), ui.surveys.map { it.id to it.reward })
        assertEquals(
            listOf(1L to R.string.my_activity_event_registered, 2L to R.string.my_activity_event_attended),
            ui.events.map { it.id to it.statusRes },
        )
        assertEquals(listOf(1L), ui.achievements.map { it.id })
        assertEquals("Искра", ui.level?.title)
        assertFalse(ui.isEmpty)
    }

    @Test
    fun `failed level and section do not break the screen`() {
        engagement.result = DataResult.Failure(DataError.Network)
        activities.quests = DataResult.Failure(DataError.Server)
        val viewModel = MyActivityViewModel(activities, achievements, engagement)

        viewModel.onScreenShown()

        val ui = viewModel.content()
        assertNull(ui.level)
        assertTrue(ui.quests.isEmpty())
        assertTrue(ui.isEmpty)
    }

    @Test
    fun `error only when every section failed, retry loads again`() {
        val failure = DataResult.Failure(DataError.Timeout)
        activities.quests = failure
        activities.surveys = failure
        activities.events = failure
        achievements.result = failure
        val viewModel = MyActivityViewModel(activities, achievements, engagement)

        viewModel.onScreenShown()
        assertEquals(MyActivityScreenState.Error(DataError.Timeout), viewModel.state.value)

        achievements.result = DataResult.Success(listOf(achievement(7, earned = true)))
        viewModel.onRetryClick()
        assertEquals(listOf(7L), viewModel.content().achievements.map { it.id })
    }

    @Test
    fun `repeated show keeps content when refresh fails`() {
        activities.surveys = DataResult.Success(listOf(survey(1, answered = true, reward = 0)))
        val viewModel = MyActivityViewModel(activities, achievements, engagement)
        viewModel.onScreenShown()

        val failure = DataResult.Failure(DataError.Network)
        activities.quests = failure
        activities.surveys = failure
        activities.events = failure
        achievements.result = failure
        viewModel.onScreenShown()

        assertEquals(listOf(1L to null), viewModel.content().surveys.map { it.id to it.reward })
    }

    private fun quest(id: Long, status: QuestParticipationStatus?) =
        Quest(id, "Квест $id", null, rewardAmount = 50, stepsCount = 3, participationStatus = status)

    private fun survey(id: Long, answered: Boolean, reward: Long) =
        Survey(id, "Опрос $id", null, questionsCount = 1, hasResponded = answered, canRespond = !answered, rewardAmount = reward)

    private fun event(id: Long, status: EventRegistrationStatus?) =
        Event(id, "Мероприятие $id", null, startAt = null, location = null, rewardAmount = 40, registrationStatus = status)

    private fun achievement(id: Long, earned: Boolean) = Achievement(
        id = id, name = "Достижение $id", description = "Описание", rewardAmount = 0,
        conditionType = AchievementCondition.Other, isEarned = earned, earnedAt = null,
    )
}

private class MyActivityFakeEngagement : EngagementRepository {
    var result: DataResult<ActivityLevel> = DataResult.Success(
        ActivityLevel(
            code = "spark", title = "Искра", rank = 1, score = 7.6, maxScore = 100.0,
            nextTitle = "Мерцание", pointsToNext = 10.4, progress = 0.42,
        ),
    )

    override suspend fun getActivityLevel() = result
}

private class MyActivityFakeAchievements : AchievementsRepository {
    var result: DataResult<List<Achievement>> = DataResult.Success(emptyList())
    override suspend fun getAchievements() = result
}

private class MyActivityFakeRepository : ActivitiesRepository {
    var quests: DataResult<List<Quest>> = DataResult.Success(emptyList())
    var surveys: DataResult<List<Survey>> = DataResult.Success(emptyList())
    var events: DataResult<List<Event>> = DataResult.Success(emptyList())

    private fun <T> DataResult<List<T>>.page(page: Int): DataResult<ActivityPage<T>> = when (this) {
        is DataResult.Success -> DataResult.Success(ActivityPage(data, page, hasNextPage = false))
        is DataResult.Failure -> this
    }

    override suspend fun getSurveys(page: Int) = surveys.page(page)
    override suspend fun getQuests(page: Int) = quests.page(page)
    override suspend fun getEvents(page: Int) = events.page(page)
    override suspend fun getSurveyDetails(surveyId: Long): DataResult<SurveyDetails> = error("не нужен")
    override suspend fun getQuestDetails(questId: Long): DataResult<QuestDetails> = error("не нужен")
    override suspend fun getEventDetails(eventId: Long): DataResult<EventDetails> = error("не нужен")
    override suspend fun submitSurvey(surveyId: Long, answers: Map<Long, SurveyAnswer>): ActivityActionResult = error("не нужен")
    override suspend fun joinQuest(questId: Long): ActivityActionResult = error("не нужен")
    override suspend fun completeQuestStep(questId: Long, stepId: Long): ActivityActionResult = error("не нужен")
    override suspend fun completeQuest(questId: Long): ActivityActionResult = error("не нужен")
    override suspend fun registerForEvent(eventId: Long): ActivityActionResult = error("не нужен")
    override suspend fun cancelEventRegistration(eventId: Long): ActivityActionResult = error("не нужен")
    override suspend fun claimEventReward(eventId: Long): ActivityActionResult = error("не нужен")
}
