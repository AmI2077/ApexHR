package com.example.apexhr.features.activities.ui.event.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError

data class EventDetailsUiModel(
    val title: String?,
    val description: String?,
    /** «23.10.2026 01:46 – 31.10.2026 01:46», только начало или null. */
    val period: String?,
    val location: String?,
    val registeredCount: Int,
    /** null — без ограничения мест. */
    val maxParticipants: Int?,
    /** Награда в апексах; null — награды нет. */
    val reward: Long?,
    @StringRes val statusRes: Int?,
    /** Доступное действие; null — кнопки нет, см. [noteRes]. */
    val action: EventAction?,
    /** Почему действия нет (запись закрыта, мест нет, награда получена). */
    @StringRes val noteRes: Int?,
) {
    companion object {
        val Preview = EventDetailsUiModel(
            title = "Тестовое мероприятие",
            description = "Встреча команды в офисе",
            period = "23.10.2026 01:46 – 31.10.2026 01:46",
            location = "ул. Примерная, д. 1",
            registeredCount = 3,
            maxParticipants = 5,
            reward = 100,
            statusRes = R.string.activities_status_event_attended,
            action = EventAction.ClaimReward,
            noteRes = null,
        )
    }
}

enum class EventAction { Register, CancelRegistration, ClaimReward }

sealed interface EventDetailsScreenState {
    data object Loading : EventDetailsScreenState
    data class Error(val error: DataError) : EventDetailsScreenState

    /** @param actionInProgress идёт запрос действия: кнопка неактивна. */
    data class Content(
        val event: EventDetailsUiModel,
        val actionInProgress: Boolean,
    ) : EventDetailsScreenState
}

interface EventDetailsScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()
    fun onActionClick(action: EventAction)

    companion object {
        val Empty = object : EventDetailsScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onActionClick(action: EventAction) = Unit
        }
    }
}
