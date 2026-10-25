package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.data.dto.ActivityPageDto
import com.example.apexhr.features.activities.data.dto.ActivityQuestListItemDto
import com.example.apexhr.features.activities.data.dto.CompleteQuestStepRequest
import com.example.apexhr.features.activities.data.dto.EventDetailsDto
import com.example.apexhr.features.activities.data.dto.EventListItemDto
import com.example.apexhr.features.activities.data.dto.QuestDetailsDto
import com.example.apexhr.features.activities.data.dto.SubmitSurveyRequest
import com.example.apexhr.features.activities.data.dto.SurveyDetailsDto
import com.example.apexhr.features.activities.data.dto.SurveyListItemDto
import com.example.apexhr.features.activities.domain.ActivityActionError
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.domain.SurveyAnswer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.SocketTimeoutException

class ActivitiesRepositoryImplTest {

    private val api = FakeActivitiesApi()
    private val repository = ActivitiesRepositoryImpl(api)

    @Test
    fun `lists request first page`() = runBlocking {
        api.surveys = listOf(SurveyListItemDto(id = 1, title = "Анкета", questionsCount = 3))
        api.quests = listOf(ActivityQuestListItemDto(id = 2, title = "Квест"))
        api.events = listOf(EventListItemDto(id = 3, title = "Встреча"))

        val surveys = (repository.getSurveys() as DataResult.Success).data
        val quests = (repository.getQuests() as DataResult.Success).data
        val events = (repository.getEvents() as DataResult.Success).data

        assertEquals(listOf(1L), surveys.map { it.id })
        assertEquals(listOf(2L), quests.map { it.id })
        assertEquals(listOf(3L), events.map { it.id })
        assertEquals(listOf(1 to 50, 1 to 50, 1 to 50), api.requestedPages)
    }

    @Test
    fun `list failures are mapped`() = runBlocking {
        api.loadError = ApiStatusException(500)
        assertEquals(DataResult.Failure(DataError.Server), repository.getSurveys())

        api.loadError = SocketTimeoutException()
        assertEquals(DataResult.Failure(DataError.Timeout), repository.getEvents())
    }

    @Test
    fun `details are loaded by id`() = runBlocking {
        val quest = (repository.getQuestDetails(7) as DataResult.Success).data
        val event = (repository.getEventDetails(8) as DataResult.Success).data
        val survey = (repository.getSurveyDetails(9) as DataResult.Success).data

        assertEquals(7L, quest.id)
        assertEquals(8L, event.id)
        assertEquals(9L, survey.id)
    }

    @Test
    fun `missing details are a load error`() = runBlocking {
        api.loadError = ApiStatusException(404)

        assertEquals(DataResult.Failure(DataError.Unknown), repository.getQuestDetails(999))
    }

    @Test
    fun `survey answers are sent`() = runBlocking {
        val result = repository.submitSurvey(
            surveyId = 3,
            answers = mapOf(11L to SurveyAnswer.SingleChoice("Офис"), 10L to SurveyAnswer.Rating(5)),
        )

        assertEquals(ActivityActionResult.Success, result)
        assertEquals(3L, api.submitted?.first)
        assertEquals(listOf(10L, 11L), api.submitted?.second?.answers?.map { it.questionId })
        assertEquals(JsonPrimitive(5), api.submitted?.second?.answers?.first()?.value)
    }

    @Test
    fun `manual step is completed without proof`() = runBlocking {
        assertEquals(ActivityActionResult.Success, repository.completeQuestStep(questId = 1, stepId = 2))

        assertEquals(Triple(1L, 2L, CompleteQuestStepRequest()), api.completedStep)
        assertNull(api.completedStep?.third?.storagePath)
    }

    @Test
    fun `actions call their endpoints`() = runBlocking {
        repository.joinQuest(1)
        repository.completeQuest(1)
        repository.registerForEvent(2)
        repository.cancelEventRegistration(2)
        repository.claimEventReward(2)

        assertEquals(
            listOf("join 1", "complete 1", "register 2", "cancel 2", "reward 2"),
            api.actions,
        )
    }

    @Test
    fun `server refusal is action failure`() = runBlocking {
        api.actionError = ActivitiesApiException(409, "Event is full.")

        assertEquals(
            ActivityActionResult.Failure(ActivityActionError.Rejected("Event is full.")),
            repository.registerForEvent(2),
        )
    }

    @Test
    fun `network failure of action is data error`() = runBlocking {
        api.actionError = SocketTimeoutException()

        assertEquals(
            ActivityActionResult.Failure(ActivityActionError.Data(DataError.Timeout)),
            repository.joinQuest(1),
        )
    }
}

private class FakeActivitiesApi : ActivitiesApi {
    var surveys = emptyList<SurveyListItemDto>()
    var quests = emptyList<ActivityQuestListItemDto>()
    var events = emptyList<EventListItemDto>()
    var loadError: Throwable? = null
    var actionError: Throwable? = null

    val requestedPages = mutableListOf<Pair<Int, Int>>()
    val actions = mutableListOf<String>()
    var submitted: Pair<Long, SubmitSurveyRequest>? = null
    var completedStep: Triple<Long, Long, CompleteQuestStepRequest>? = null

    private fun <T> pageOf(page: Int, pageSize: Int, items: List<T>): ActivityPageDto<T> {
        loadError?.let { throw it }
        requestedPages += page to pageSize
        return ActivityPageDto(items = items, totalCount = items.size)
    }

    override suspend fun getSurveys(page: Int, pageSize: Int) = pageOf(page, pageSize, surveys)
    override suspend fun getQuests(page: Int, pageSize: Int) = pageOf(page, pageSize, quests)
    override suspend fun getEvents(page: Int, pageSize: Int) = pageOf(page, pageSize, events)

    override suspend fun getSurveyDetails(surveyId: Long): SurveyDetailsDto {
        loadError?.let { throw it }
        return SurveyDetailsDto(id = surveyId)
    }

    override suspend fun getQuestDetails(questId: Long): QuestDetailsDto {
        loadError?.let { throw it }
        return QuestDetailsDto(id = questId)
    }

    override suspend fun getEventDetails(eventId: Long): EventDetailsDto {
        loadError?.let { throw it }
        return EventDetailsDto(id = eventId)
    }

    override suspend fun submitSurvey(surveyId: Long, request: SubmitSurveyRequest) {
        actionError?.let { throw it }
        submitted = surveyId to request
    }

    override suspend fun joinQuest(questId: Long) = act("join $questId")

    override suspend fun completeQuestStep(questId: Long, stepId: Long, request: CompleteQuestStepRequest) {
        actionError?.let { throw it }
        completedStep = Triple(questId, stepId, request)
    }

    override suspend fun completeQuest(questId: Long) = act("complete $questId")
    override suspend fun registerForEvent(eventId: Long) = act("register $eventId")
    override suspend fun cancelEventRegistration(eventId: Long) = act("cancel $eventId")
    override suspend fun claimEventReward(eventId: Long) = act("reward $eventId")

    private fun act(name: String) {
        actionError?.let { throw it }
        actions += name
    }
}
