package com.example.apexhr.features.activities.ui.event.models

import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.EventDetails
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.ui.models.formatActivityDate
import com.example.apexhr.features.activities.ui.models.labelRes
import com.example.apexhr.features.activities.ui.models.toRewardOrNull
import java.time.ZoneId

/** @param zone часовой пояс, в котором показываем даты (по умолчанию — устройства). */
fun EventDetails.toUiModel(zone: ZoneId = ZoneId.systemDefault()): EventDetailsUiModel {
    val start = startAt?.formatActivityDate(zone)
    val end = endAt?.formatActivityDate(zone)
    val (action, noteRes) = availableAction()
    return EventDetailsUiModel(
        title = title,
        description = description,
        period = when {
            start != null && end != null -> "$start – $end"
            else -> start
        },
        location = location,
        registeredCount = registeredCount,
        maxParticipants = maxParticipants,
        reward = rewardAmount.toRewardOrNull(),
        statusRes = registration?.status?.labelRes(),
        action = action,
        noteRes = noteRes,
    )
}

/**
 * Что можно сделать с мероприятием. Окончательно решает сервер: при отказе
 * его текст покажется в snackbar.
 */
private fun EventDetails.availableAction(): Pair<EventAction?, Int?> {
    val registration = registration
    return when (registration?.status) {
        EventRegistrationStatus.Registered -> EventAction.CancelRegistration to null

        EventRegistrationStatus.Attended -> when {
            rewardAmount <= 0 -> null to null
            registration.rewardClaimed -> null to R.string.event_note_reward_claimed
            else -> EventAction.ClaimReward to null
        }

        EventRegistrationStatus.NoShow, EventRegistrationStatus.Unknown -> null to null

        // Записи нет или она отменена: можно записаться, если запись открыта и есть места.
        null, EventRegistrationStatus.Cancelled -> when {
            !isOpen -> null to R.string.event_note_closed
            maxParticipants != null && registeredCount >= maxParticipants ->
                null to R.string.event_note_full

            else -> EventAction.Register to null
        }
    }
}
