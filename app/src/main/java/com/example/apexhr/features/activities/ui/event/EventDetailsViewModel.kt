package com.example.apexhr.features.activities.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.common.toActivityMessage
import com.example.apexhr.features.activities.ui.event.models.EventAction
import com.example.apexhr.features.activities.ui.event.models.EventDetailsScreenCallbacks
import com.example.apexhr.features.activities.ui.event.models.EventDetailsScreenState
import com.example.apexhr.features.activities.ui.event.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Детали мероприятия: запись, отмена записи (без подтверждения) и получение награды.
 * После любого действия детали перечитываются. «Назад» задаёт навигация.
 */
class EventDetailsViewModel(
    private val eventId: Long,
    private val repository: ActivitiesRepository,
) : ViewModel(), EventDetailsScreenCallbacks {

    private val _state = MutableStateFlow<EventDetailsScreenState>(EventDetailsScreenState.Loading)
    val state: StateFlow<EventDetailsScreenState> = _state.asStateFlow()

    private val _messages = Channel<ActivityMessage>(Channel.BUFFERED)
    val messages: Flow<ActivityMessage> = _messages.receiveAsFlow()

    private var loadJob: Job? = null
    private var actionInProgress = false

    init {
        load()
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()

    override fun onActionClick(action: EventAction) {
        val content = _state.value as? EventDetailsScreenState.Content ?: return
        if (actionInProgress) return
        actionInProgress = true
        _state.value = content.copy(actionInProgress = true)
        viewModelScope.launch {
            val result = when (action) {
                EventAction.Register -> repository.registerForEvent(eventId)
                EventAction.CancelRegistration -> repository.cancelEventRegistration(eventId)
                EventAction.ClaimReward -> repository.claimEventReward(eventId)
            }
            val message = when (result) {
                ActivityActionResult.Success -> ActivityMessage(action.successRes())
                is ActivityActionResult.Failure -> result.error.toActivityMessage()
            }
            _messages.send(message)
            // И после отказа: запись или места могли измениться.
            reload()
        }
    }

    /** Перечитывает детали; при ошибке уже показанные данные остаются. */
    private suspend fun reload() {
        val result = repository.getEventDetails(eventId)
        actionInProgress = false
        val current = _state.value
        _state.value = when (result) {
            is DataResult.Success -> EventDetailsScreenState.Content(result.data.toUiModel(), actionInProgress = false)
            is DataResult.Failure -> if (current is EventDetailsScreenState.Content) {
                current.copy(actionInProgress = false)
            } else {
                EventDetailsScreenState.Error(result.error)
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = EventDetailsScreenState.Loading
        loadJob = viewModelScope.launch { reload() }
    }
}

private fun EventAction.successRes(): Int = when (this) {
    EventAction.Register -> R.string.event_message_registered
    EventAction.CancelRegistration -> R.string.event_message_cancelled
    EventAction.ClaimReward -> R.string.event_message_reward_claimed
}
