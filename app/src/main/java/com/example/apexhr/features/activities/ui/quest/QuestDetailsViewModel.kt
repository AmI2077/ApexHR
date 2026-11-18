package com.example.apexhr.features.activities.ui.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.common.toActivityMessage
import com.example.apexhr.features.activities.ui.quest.models.QuestAction
import com.example.apexhr.features.activities.ui.quest.models.QuestDetailsScreenCallbacks
import com.example.apexhr.features.activities.ui.quest.models.QuestDetailsScreenState
import com.example.apexhr.features.activities.ui.quest.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Детали квеста: присоединиться, отметить ручной шаг, завершить квест.
 * После любого действия детали перечитываются. «Назад» задаёт навигация.
 */
class QuestDetailsViewModel(
    private val questId: Long,
    private val repository: ActivitiesRepository,
) : ViewModel(), QuestDetailsScreenCallbacks {

    private val _state = MutableStateFlow<QuestDetailsScreenState>(QuestDetailsScreenState.Loading)
    val state: StateFlow<QuestDetailsScreenState> = _state.asStateFlow()

    private val _messages = Channel<ActivityMessage>(Channel.BUFFERED)
    val messages: Flow<ActivityMessage> = _messages.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()

    override fun onActionClick(action: QuestAction) {
        val content = _state.value as? QuestDetailsScreenState.Content ?: return
        if (content.pendingAction != null) return
        _state.value = content.copy(pendingAction = action)
        viewModelScope.launch {
            val result = when (action) {
                QuestAction.Join -> repository.joinQuest(questId)
                is QuestAction.CompleteStep -> repository.completeQuestStep(questId, action.stepId)
                QuestAction.Complete -> repository.completeQuest(questId)
            }
            val message = when (result) {
                ActivityActionResult.Success -> ActivityMessage(action.successRes())
                is ActivityActionResult.Failure -> result.error.toActivityMessage()
            }
            _messages.send(message)
            // И после отказа: шаги или статус могли измениться.
            reload()
        }
    }

    /**
     * Тихо перечитывает уже показанный квест, например после возврата с экрана
     * подтверждения шага. Пока идёт первая загрузка или действие, ничего не делает.
     */
    fun refresh() {
        val content = _state.value as? QuestDetailsScreenState.Content ?: return
        if (content.pendingAction != null || loadJob?.isActive == true) return
        loadJob = viewModelScope.launch { reload() }
    }

    /** Перечитывает детали; при ошибке уже показанные данные остаются. */
    private suspend fun reload() {
        val result = repository.getQuestDetails(questId)
        val current = _state.value
        _state.value = when (result) {
            is DataResult.Success -> QuestDetailsScreenState.Content(result.data.toUiModel(), pendingAction = null)
            is DataResult.Failure -> if (current is QuestDetailsScreenState.Content) {
                current.copy(pendingAction = null)
            } else {
                QuestDetailsScreenState.Error(result.error)
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = QuestDetailsScreenState.Loading
        loadJob = viewModelScope.launch { reload() }
    }
}

private fun QuestAction.successRes(): Int = when (this) {
    QuestAction.Join -> R.string.quest_message_joined
    is QuestAction.CompleteStep -> R.string.quest_message_step_completed
    QuestAction.Complete -> R.string.quest_message_completed
}
