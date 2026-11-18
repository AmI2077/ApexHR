package com.example.apexhr.features.support.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.support.domain.SupportRepository
import com.example.apexhr.features.support.domain.SupportRepository.Companion.MESSAGE_MAX_LENGTH
import com.example.apexhr.features.support.ui.models.SupportTicketScreenCallbacks
import com.example.apexhr.features.support.ui.models.SupportTicketScreenState
import com.example.apexhr.features.support.ui.models.SupportTicketUiState
import com.example.apexhr.features.support.ui.models.toUiModel
import com.example.apexhr.features.support.ui.models.toUiState
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
 * Обращение с перепиской и полем ответа. Отправленное сообщение добавляется
 * в конец переписки, поле очищается. «Назад» задаёт навигация.
 */
class SupportTicketViewModel(
    private val ticketId: Long,
    private val repository: SupportRepository,
) : ViewModel(), SupportTicketScreenCallbacks {

    private val _state = MutableStateFlow<SupportTicketScreenState>(SupportTicketScreenState.Loading)
    val state: StateFlow<SupportTicketScreenState> = _state.asStateFlow()

    private val _messages = Channel<Int>(Channel.BUFFERED)

    /** Разовые сообщения об ошибках (id строки). */
    val messages: Flow<Int> = _messages.receiveAsFlow()

    private var loadJob: Job? = null
    private var ticketAuthorId: String? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = SupportTicketScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = repository.getTicket(ticketId)) {
                is DataResult.Success -> {
                    ticketAuthorId = result.data.authorId
                    SupportTicketScreenState.Content(result.data.toUiState())
                }

                is DataResult.Failure -> SupportTicketScreenState.Error(result.error)
            }
        }
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()

    override fun onReplyChange(text: String) {
        updateContent { it.copy(reply = text.take(MESSAGE_MAX_LENGTH)) }
    }

    override fun onSendClick() {
        val current = content() ?: return
        if (!current.canSend) return
        val text = current.reply.trim()
        updateContent { it.copy(isSending = true) }
        viewModelScope.launch {
            when (val result = repository.sendMessage(ticketId, text)) {
                is DataResult.Success -> {
                    val message = result.data.toUiModel(ticketAuthorId)
                    updateContent { state ->
                        state.copy(
                            messages = state.messages.filterNot { it.id == message.id } + message,
                            reply = "",
                            isSending = false,
                        )
                    }
                }

                is DataResult.Failure -> {
                    updateContent { it.copy(isSending = false) }
                    _messages.send(result.error.messageRes())
                }
            }
        }
    }

    private fun content(): SupportTicketUiState? =
        (_state.value as? SupportTicketScreenState.Content)?.uiState

    private fun updateContent(transform: (SupportTicketUiState) -> SupportTicketUiState) {
        _state.update { state ->
            if (state is SupportTicketScreenState.Content) SupportTicketScreenState.Content(transform(state.uiState)) else state
        }
    }
}