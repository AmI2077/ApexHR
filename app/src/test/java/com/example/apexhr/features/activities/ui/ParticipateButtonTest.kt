package com.example.apexhr.features.activities.ui

import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.activities.ui.models.toUiModel
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

/** Кнопка «Участвовать» на карточке — только когда участие возможно, как в вебе. */
class ParticipateButtonTest {

    private fun quest(status: QuestParticipationStatus?) = Quest(
        id = 1, title = "Квест", description = null, rewardAmount = 10, stepsCount = 2, participationStatus = status,
    )

    private fun event(status: EventRegistrationStatus?) = Event(
        id = 1, title = "Событие", description = null, startAt = null, location = null,
        rewardAmount = 10, registrationStatus = status,
    )

    private fun survey(canRespond: Boolean) = Survey(
        id = 1, title = "Опрос", description = null, questionsCount = 3, hasResponded = !canRespond, canRespond = canRespond,
    )

    @Test
    fun `quest can be joined only when user is not in it`() {
        val expected = mapOf(
            null to true,
            QuestParticipationStatus.Cancelled to true,
            QuestParticipationStatus.Unknown to true,
            QuestParticipationStatus.InProgress to false,
            QuestParticipationStatus.Completed to false,
        )
        expected.forEach { (status, can) -> assertEquals("$status", can, quest(status).toUiModel().canParticipate) }
    }

    @Test
    fun `event can be joined only without active registration`() {
        val expected = mapOf(
            null to true,
            EventRegistrationStatus.Cancelled to true,
            EventRegistrationStatus.Registered to false,
            EventRegistrationStatus.Attended to false,
            EventRegistrationStatus.NoShow to false,
            EventRegistrationStatus.Unknown to false,
        )
        expected.forEach { (status, can) ->
            assertEquals("$status", can, event(status).toUiModel(ZoneOffset.UTC).canParticipate)
        }
    }

    @Test
    fun `survey can be taken only while answers are accepted`() {
        assertEquals(true, survey(canRespond = true).toUiModel().canParticipate)
        assertEquals(false, survey(canRespond = false).toUiModel().canParticipate)
    }
}