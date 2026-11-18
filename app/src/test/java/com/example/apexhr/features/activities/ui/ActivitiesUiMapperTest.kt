package com.example.apexhr.features.activities.ui

import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.activities.ui.models.ActivityKind
import com.example.apexhr.features.activities.ui.models.ActivitySubtitle
import com.example.apexhr.features.activities.ui.models.ActivityUiModel
import com.example.apexhr.features.activities.ui.models.filterByQuery
import com.example.apexhr.features.activities.ui.models.labelRes
import com.example.apexhr.features.activities.ui.models.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ActivitiesUiMapperTest {

    private val moscow = ZoneId.of("Europe/Moscow")

    private fun event(startAt: Instant? = null, location: String? = null, reward: Long = 0) = Event(
        id = 1,
        title = "Тестовое мероприятие",
        description = null,
        startAt = startAt,
        location = location,
        rewardAmount = reward,
        registrationStatus = null,
    )

    @Test
    fun `survey shows questions and no reward`() {
        val survey = Survey(
            id = 3,
            title = "Пульс команды",
            description = null,
            questionsCount = 4,
            hasResponded = true,
            canRespond = false,
        )

        assertEquals(
            ActivityUiModel(
                id = 3,
                kind = ActivityKind.Survey,
                title = "Пульс команды",
                subtitle = ActivitySubtitle.Questions(4),
                reward = null,
                statusRes = R.string.activities_status_survey_answered,
                canParticipate = false,
            ),
            survey.toUiModel(),
        )
        assertNull(survey.copy(hasResponded = false).toUiModel().statusRes)
    }

    @Test
    fun `quest shows steps reward and participation`() {
        val quest = Quest(
            id = 1,
            title = "Тестовый квест",
            description = null,
            rewardAmount = 1500,
            stepsCount = 2,
            participationStatus = QuestParticipationStatus.InProgress,
        )

        assertEquals(
            ActivityUiModel(
                id = 1,
                kind = ActivityKind.Quest,
                title = "Тестовый квест",
                subtitle = ActivitySubtitle.Steps(2),
                reward = 1500L,
                statusRes = R.string.activities_status_quest_in_progress,
                canParticipate = false,
            ),
            quest.toUiModel(),
        )
        assertNull(quest.copy(rewardAmount = 0, participationStatus = null).toUiModel().reward)
    }

    @Test
    fun `event shows local date and location`() {
        val model = event(
            startAt = Instant.parse("2026-10-22T22:46:50.474Z"),
            location = "ул. Примерная, д. 1",
            reward = 100,
        ).copy(registrationStatus = EventRegistrationStatus.Attended).toUiModel(moscow)

        assertEquals(ActivityKind.Event, model.kind)
        assertEquals(ActivitySubtitle.EventTime("23.10.2026 01:46", "ул. Примерная, д. 1"), model.subtitle)
        assertEquals(100L, model.reward)
        assertEquals(R.string.activities_status_event_attended, model.statusRes)
    }

    @Test
    fun `event without date and place has no subtitle`() {
        assertNull(event().toUiModel(moscow).subtitle)
        assertEquals(
            ActivitySubtitle.EventTime(startAt = null, location = "Офис"),
            event(location = "Офис").toUiModel(moscow).subtitle,
        )
    }

    @Test
    fun `statuses have labels and unknown has none`() {
        assertEquals(R.string.activities_status_quest_completed, QuestParticipationStatus.Completed.labelRes())
        assertNull(QuestParticipationStatus.Unknown.labelRes())
        assertEquals(R.string.activities_status_event_registered, EventRegistrationStatus.Registered.labelRes())
        assertEquals(R.string.activities_status_event_no_show, EventRegistrationStatus.NoShow.labelRes())
        assertNull(EventRegistrationStatus.Unknown.labelRes())
    }

    @Test
    fun `search filters by title ignoring case`() {
        val items = listOf(
            ActivityUiModel(1, ActivityKind.Quest, "Тестовый квест", null, null, null),
            ActivityUiModel(2, ActivityKind.Quest, "Забег", null, null, null),
            ActivityUiModel(3, ActivityKind.Quest, null, null, null, null),
        )

        assertEquals(listOf(1L), items.filterByQuery("  тестовый ").map { it.id })
        assertEquals(listOf(1L, 2L, 3L), items.filterByQuery(" ").map { it.id })
        assertEquals(emptyList<Long>(), items.filterByQuery("опрос").map { it.id })
    }
}
