package com.example.apexhr.features.activities.data

import com.example.apexhr.features.activities.data.dto.ActivityPageDto
import com.example.apexhr.features.activities.data.dto.ActivityQuestListItemDto
import com.example.apexhr.features.activities.data.dto.EventDetailsDto
import com.example.apexhr.features.activities.data.dto.EventListItemDto
import com.example.apexhr.features.activities.data.dto.QuestDetailsDto
import com.example.apexhr.features.activities.data.dto.SurveyDetailsDto
import com.example.apexhr.features.activities.data.dto.SurveyListItemDto
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.domain.ProofStatus
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.QuestStepType
import com.example.apexhr.features.activities.domain.SurveyQuestionType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ActivitiesMappersTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `real quest list response is parsed`() {
        val body = """{"items":[{"id":1,"title":"Тестовый квест","description":"Тестовый квест с 3 шагами",
            "rewardAmount":100,"startAt":"2026-10-23T19:38:22.939Z","endAt":"2026-10-31T19:38:22.939Z",
            "stepsCount":2,"requiredStepsCount":2,"participationStatus":null}],
            "totalCount":1,"page":1,"pageSize":50,"totalPages":1,"hasPreviousPage":false,"hasNextPage":false}"""

        val page = json.decodeFromString<ActivityPageDto<ActivityQuestListItemDto>>(body)
        val quest = page.items.single().toDomain()

        assertEquals(1, page.totalCount)
        assertEquals(1L, quest.id)
        assertEquals("Тестовый квест", quest.title)
        assertEquals(100L, quest.rewardAmount)
        assertEquals(2, quest.stepsCount)
        assertNull(quest.participationStatus)
    }

    @Test
    fun `real quest details response is parsed`() {
        val body = """{"id":1,"title":"Тестовый квест","description":"Тестовый квест с 3 шагами",
            "rewardAmount":100,"isActive":true,"startAt":"2026-10-23T19:38:22.939Z",
            "endAt":"2026-10-31T19:38:22.939Z","participation":null,"steps":[
            {"id":2,"title":"Второй шаг","description":"Загрузите фото","completionType":"proof",
             "sortOrder":2,"isRequired":true,"isCompleted":false,"completedAt":null,"proofStatus":null,
             "proofStoragePath":null,"proofComment":null},
            {"id":1,"title":"Первый шаг","description":"Выполните первый шаг","completionType":"manual",
             "sortOrder":1,"isRequired":true,"isCompleted":false,"completedAt":null,"proofStatus":null,
             "proofStoragePath":null,"proofComment":null}]}"""

        val quest = json.decodeFromString<QuestDetailsDto>(body).toDomain()

        assertNull(quest.participation)
        assertEquals(Instant.parse("2026-10-31T19:38:22.939Z"), quest.endAt)
        assertEquals(listOf(1L, 2L), quest.steps.map { it.id })
        assertEquals(QuestStepType.Manual, quest.steps[0].completionType)
        assertEquals(QuestStepType.Proof, quest.steps[1].completionType)
        assertNull(quest.steps[1].proofStatus)
    }

    @Test
    fun `quest participation and proof statuses are mapped`() {
        val body = """{"id":5,"participation":{"id":3,"status":"in_progress","startedAt":"2026-10-24T10:00:00Z",
            "rewardClaimed":false},"steps":[{"id":7,"completionType":"proof","isCompleted":false,
            "proofStatus":"pending"},{"id":8,"completionType":"teleport","proofStatus":"lost"}]}"""

        val quest = json.decodeFromString<QuestDetailsDto>(body).toDomain()

        assertEquals(QuestParticipationStatus.InProgress, quest.participation?.status)
        assertEquals(ProofStatus.Pending, quest.steps[0].proofStatus)
        assertEquals(QuestStepType.Unknown, quest.steps[1].completionType)
        assertEquals(ProofStatus.Unknown, quest.steps[1].proofStatus)
    }

    @Test
    fun `quest list participation status is mapped`() {
        val quest = ActivityQuestListItemDto(id = 1, participationStatus = " Completed ").toDomain()

        assertEquals(QuestParticipationStatus.Completed, quest.participationStatus)
    }

    @Test
    fun `real event list response is parsed`() {
        val body = """{"items":[{"id":1,"title":"Тестовое мероприятие","description":"Тестовое мероприятие",
            "startAt":"2026-10-22T22:46:50.474Z","endAt":"2026-10-31T22:46:50.474Z",
            "location":"ул. Примерная, д. 1","maxParticipants":5,"registeredCount":3,"rewardAmount":100,
            "registrationStatus":"attended"}],"totalCount":1,"page":1,"pageSize":50,"totalPages":1,
            "hasPreviousPage":false,"hasNextPage":false}"""

        val event = json.decodeFromString<ActivityPageDto<EventListItemDto>>(body).items.single().toDomain()

        assertEquals("ул. Примерная, д. 1", event.location)
        assertEquals(Instant.parse("2026-10-22T22:46:50.474Z"), event.startAt)
        assertEquals(EventRegistrationStatus.Attended, event.registrationStatus)
        assertEquals(100L, event.rewardAmount)
    }

    @Test
    fun `real event details response is parsed`() {
        val body = """{"id":1,"title":"Тестовое мероприятие","description":"Тестовое мероприятие",
            "startAt":"2026-10-22T22:46:50.474Z","endAt":"2026-10-31T22:46:50.474Z",
            "location":"ул. Примерная, д. 1","maxParticipants":5,"registeredCount":3,"rewardAmount":100,
            "isActive":true,"isOpen":true,"registration":{"id":2,
            "userId":"00000000-0000-0000-0000-000000000000","firstName":"","lastName":"","avatarUrl":null,
            "status":"attended","registeredAt":"2026-10-25T14:35:59.681576Z",
            "attendedAt":"2026-10-25T14:40:15.442168Z","rewardClaimed":false}}"""

        val event = json.decodeFromString<EventDetailsDto>(body).toDomain()

        assertTrue(event.isOpen)
        assertEquals(5, event.maxParticipants)
        assertEquals(3, event.registeredCount)
        assertEquals(EventRegistrationStatus.Attended, event.registration?.status)
        assertEquals(false, event.registration?.rewardClaimed)
    }

    @Test
    fun `inactive event is not open and empty fields are null`() {
        val event = EventDetailsDto(
            id = 1,
            title = "  ",
            location = "",
            maxParticipants = 0,
            isActive = false,
            isOpen = true,
            registration = null,
        ).toDomain()

        assertFalse(event.isOpen)
        assertNull(event.title)
        assertNull(event.location)
        assertNull(event.maxParticipants)
        assertNull(event.registration)
    }

    @Test
    fun `survey list item from swagger is parsed`() {
        val body = """{"items":[{"id":3,"title":"Пульс команды","description":null,"startAt":null,
            "endAt":"2026-11-01T00:00:00Z","allowMultipleResponses":false,"questionsCount":4,
            "hasResponded":true,"canRespond":false,"lastSubmittedAt":"2026-10-20T08:00:00Z"}],"totalCount":1}"""

        val survey = json.decodeFromString<ActivityPageDto<SurveyListItemDto>>(body).items.single().toDomain()

        assertEquals(4, survey.questionsCount)
        assertTrue(survey.hasResponded)
        assertFalse(survey.canRespond)
    }

    @Test
    fun `survey details from swagger are parsed`() {
        val body = """{"id":3,"title":"Пульс команды","isActive":true,"allowMultipleResponses":false,
            "canRespond":true,"questions":[
            {"id":12,"questionText":"Что улучшить?","questionType":"text","options":null,"isRequired":false,"sortOrder":3},
            {"id":10,"questionText":"Оцените неделю","questionType":"rating","isRequired":true,"sortOrder":1},
            {"id":11,"questionText":"Формат","questionType":"single_choice","options":["Офис"," ","Удалёнка"],
             "isRequired":true,"sortOrder":2},
            {"id":13,"questionText":"Голограмма","questionType":"hologram","sortOrder":4}],
            "responses":[{"id":1,"surveyId":3,"submittedAt":"2026-10-20T08:00:00Z",
            "answers":[{"questionId":10,"value":5},{"questionId":11,"value":"Офис"}]}]}"""

        val survey = json.decodeFromString<SurveyDetailsDto>(body).toDomain()

        assertEquals(listOf(10L, 11L, 12L, 13L), survey.questions.map { it.id })
        assertEquals(SurveyQuestionType.Rating, survey.questions[0].type)
        assertEquals(listOf("Офис", "Удалёнка"), survey.questions[1].options)
        assertEquals(SurveyQuestionType.Text, survey.questions[2].type)
        assertEquals(SurveyQuestionType.Unknown, survey.questions[3].type)
        assertTrue(survey.hasResponded)
        assertEquals(Instant.parse("2026-10-20T08:00:00Z"), survey.lastSubmittedAt)
        assertTrue(survey.canRespond)
    }

    @Test
    fun `inactive survey cannot be answered`() {
        val survey = SurveyDetailsDto(id = 1, isActive = false, canRespond = true).toDomain()

        assertFalse(survey.canRespond)
        assertFalse(survey.hasResponded)
        assertNull(survey.lastSubmittedAt)
    }

    @Test
    fun `dates without zone are utc and broken dates are null`() {
        assertEquals(Instant.parse("2026-10-23T19:38:22Z"), "2026-10-23T19:38:22".toActivityInstantOrNull())
        assertEquals(Instant.parse("2026-10-23T16:38:22Z"), "2026-10-23T19:38:22+03:00".toActivityInstantOrNull())
        assertNull("вчера".toActivityInstantOrNull())
        assertNull("".toActivityInstantOrNull())
        assertNull(null.toActivityInstantOrNull())
    }
}
