package com.example.apexhr.features.activities.ui.event

import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.EventDetails
import com.example.apexhr.features.activities.domain.EventRegistration
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.ui.event.models.EventAction
import com.example.apexhr.features.activities.ui.event.models.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class EventDetailsUiMapperTest {

    private val moscow = ZoneId.of("Europe/Moscow")

    private fun event(
        status: EventRegistrationStatus? = null,
        rewardClaimed: Boolean = false,
        isOpen: Boolean = true,
        maxParticipants: Int? = 5,
        registeredCount: Int = 3,
        reward: Long = 100,
        startAt: Instant? = Instant.parse("2026-10-22T22:46:00Z"),
        endAt: Instant? = Instant.parse("2026-10-30T22:46:00Z"),
    ) = EventDetails(
        id = 1,
        title = "Тестовое мероприятие",
        description = null,
        startAt = startAt,
        endAt = endAt,
        location = "ул. Примерная, д. 1",
        maxParticipants = maxParticipants,
        registeredCount = registeredCount,
        rewardAmount = reward,
        isOpen = isOpen,
        registration = status?.let { EventRegistration(it, rewardClaimed) },
    )

    @Test
    fun `maps fields and period in given zone`() {
        val model = event().toUiModel(moscow)

        assertEquals("23.10.2026 01:46 – 31.10.2026 01:46", model.period)
        assertEquals("ул. Примерная, д. 1", model.location)
        assertEquals(3, model.registeredCount)
        assertEquals(5, model.maxParticipants)
        assertEquals("100", model.reward)
        assertNull(model.statusRes)
    }

    @Test
    fun `period shows only start without end and nothing without start`() {
        assertEquals("23.10.2026 01:46", event(endAt = null).toUiModel(moscow).period)
        assertNull(event(startAt = null).toUiModel(moscow).period)
    }

    @Test
    fun `zero reward is hidden`() {
        assertNull(event(reward = 0).toUiModel(moscow).reward)
    }

    @Test
    fun `open event with free places can be registered`() {
        val model = event().toUiModel(moscow)
        assertEquals(EventAction.Register, model.action)
        assertNull(model.noteRes)

        val unlimited = event(maxParticipants = null, registeredCount = 100).toUiModel(moscow)
        assertEquals(EventAction.Register, unlimited.action)

        val cancelled = event(status = EventRegistrationStatus.Cancelled).toUiModel(moscow)
        assertEquals(EventAction.Register, cancelled.action)
        assertEquals(R.string.activities_status_event_cancelled, cancelled.statusRes)
    }

    @Test
    fun `closed or full event shows note instead of action`() {
        val closed = event(isOpen = false).toUiModel(moscow)
        assertNull(closed.action)
        assertEquals(R.string.event_note_closed, closed.noteRes)

        val full = event(registeredCount = 5).toUiModel(moscow)
        assertNull(full.action)
        assertEquals(R.string.event_note_full, full.noteRes)
    }

    @Test
    fun `registered user can cancel registration`() {
        val model = event(status = EventRegistrationStatus.Registered, isOpen = false).toUiModel(moscow)

        assertEquals(EventAction.CancelRegistration, model.action)
        assertEquals(R.string.activities_status_event_registered, model.statusRes)
    }

    @Test
    fun `attended user claims reward once`() {
        val notClaimed = event(status = EventRegistrationStatus.Attended).toUiModel(moscow)
        assertEquals(EventAction.ClaimReward, notClaimed.action)

        val claimed = event(status = EventRegistrationStatus.Attended, rewardClaimed = true).toUiModel(moscow)
        assertNull(claimed.action)
        assertEquals(R.string.event_note_reward_claimed, claimed.noteRes)

        val noReward = event(status = EventRegistrationStatus.Attended, reward = 0).toUiModel(moscow)
        assertNull(noReward.action)
        assertNull(noReward.noteRes)
    }

    @Test
    fun `no show and unknown status have no action`() {
        listOf(EventRegistrationStatus.NoShow, EventRegistrationStatus.Unknown).forEach { status ->
            val model = event(status = status).toUiModel(moscow)
            assertNull(model.action)
            assertNull(model.noteRes)
        }
    }
}
