package com.example.apexhr.features.activities.ui.quest.proof

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ProofStatus
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.QuestProofRepository
import com.example.apexhr.features.activities.domain.QuestProofResult
import com.example.apexhr.features.activities.domain.QuestStepType
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.quest.models.QuestProofAction
import com.example.apexhr.features.activities.ui.quest.proof.models.PROOF_COMMENT_MAX_LENGTH
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofScreenCallbacks
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofScreenState
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofUiState
import com.example.apexhr.features.activities.ui.quest.proof.models.toActivityMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Подтверждение шага квеста: файл (JPEG, PNG, WebP или PDF до 2 МБ) и комментарий.
 * Что можно сделать, решает состояние шага в деталях квеста: отправить,
 * отправить заново после отказа или заменить подтверждение на проверке.
 * После успеха отдаёт событие в [sent], навигация закрывает экран.
 */
class QuestStepProofViewModel(
    private val questId: Long,
    private val stepId: Long,
    private val activitiesRepository: ActivitiesRepository,
    private val proofRepository: QuestProofRepository,
) : ViewModel(), QuestStepProofScreenCallbacks {

    private val _state = MutableStateFlow<QuestStepProofScreenState>(QuestStepProofScreenState.Loading)
    val state: StateFlow<QuestStepProofScreenState> = _state.asStateFlow()

    private val _messages = Channel<ActivityMessage>(Channel.BUFFERED)

    /** Ошибки отправки. */
    val messages: Flow<ActivityMessage> = _messages.receiveAsFlow()

    private val _sent = Channel<Unit>(Channel.BUFFERED)

    /** Подтверждение принято сервером. */
    val sent: Flow<Unit> = _sent.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = QuestStepProofScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = activitiesRepository.getQuestDetails(questId)) {
                is DataResult.Success -> result.data.toProofState(stepId)
                is DataResult.Failure -> QuestStepProofScreenState.Error(result.error)
            }
        }
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()

    override fun onFileSelected(uri: String) = updateContent { it.copy(fileUri = uri) }

    override fun onCommentChange(text: String) =
        updateContent { it.copy(comment = text.take(PROOF_COMMENT_MAX_LENGTH)) }

    override fun onSendClick() {
        val current = (_state.value as? QuestStepProofScreenState.Content)?.uiState ?: return
        if (!current.canSend) return
        updateContent { it.copy(isSending = true) }
        viewModelScope.launch {
            val result = proofRepository.submitProof(
                questId = questId,
                stepId = stepId,
                fileUri = current.fileUri,
                existingStoragePath = current.existingStoragePath,
                comment = current.comment,
                update = current.action == QuestProofAction.Update,
            )
            when (result) {
                // Кнопка остаётся неактивной: экран сейчас закроется.
                QuestProofResult.Success -> _sent.send(Unit)
                is QuestProofResult.Failure -> {
                    updateContent { it.copy(isSending = false) }
                    _messages.send(result.error.toActivityMessage())
                }
            }
        }
    }

    private fun updateContent(transform: (QuestStepProofUiState) -> QuestStepProofUiState) {
        _state.update { state ->
            if (state is QuestStepProofScreenState.Content) QuestStepProofScreenState.Content(transform(state.uiState)) else state
        }
    }
}

/** Форма доступна участнику для шага с подтверждением, который ещё не выполнен и не принят. */
internal fun QuestDetails.toProofState(stepId: Long): QuestStepProofScreenState {
    val step = steps.firstOrNull { it.id == stepId }
    if (step == null ||
        participation?.status != QuestParticipationStatus.InProgress ||
        step.completionType != QuestStepType.Proof ||
        step.isCompleted
    ) {
        return QuestStepProofScreenState.Unavailable
    }
    val action = when (step.proofStatus) {
        ProofStatus.Pending -> QuestProofAction.Update
        ProofStatus.Rejected -> QuestProofAction.Resubmit
        ProofStatus.Approved -> return QuestStepProofScreenState.Unavailable
        ProofStatus.Unknown, null -> QuestProofAction.Submit
    }
    return QuestStepProofScreenState.Content(
        QuestStepProofUiState(
            stepTitle = step.title,
            stepDescription = step.description,
            action = action,
            existingStoragePath = step.proofStoragePath,
            // При замене удобнее поправить прежний комментарий, чем писать заново.
            comment = if (action == QuestProofAction.Update) step.proofComment.orEmpty() else "",
        )
    )
}