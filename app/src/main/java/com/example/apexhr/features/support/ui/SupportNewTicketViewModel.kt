package com.example.apexhr.features.support.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.support.domain.SupportRepository
import com.example.apexhr.features.support.ui.models.SupportNewTicketScreenCallbacks
import com.example.apexhr.features.support.ui.models.SupportNewTicketUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Новое обращение: тема и описание обязательны, длина ограничена как на сервере.
 * После создания отдаёт id обращения в [createdTicketId], чтобы навигация открыла его.
 */
class SupportNewTicketViewModel(
    private val repository: SupportRepository,
) : ViewModel(), SupportNewTicketScreenCallbacks {

    private val _state = MutableStateFlow(SupportNewTicketUiState())
    val state: StateFlow<SupportNewTicketUiState> = _state.asStateFlow()

    private val _messages = Channel<Int>(Channel.BUFFERED)

    /** Разовые сообщения об ошибках (id строки). */
    val messages: Flow<Int> = _messages.receiveAsFlow()

    private val _createdTicketId = Channel<Long>(Channel.BUFFERED)

    /** id созданного обращения. */
    val createdTicketId: Flow<Long> = _createdTicketId.receiveAsFlow()

    private var created = false

    override fun onBackClick() = Unit

    override fun onSubjectChange(text: String) {
        _state.update { it.copy(subject = text.take(SupportNewTicketUiState.SUBJECT_MAX_LENGTH)) }
    }

    override fun onMessageChange(text: String) {
        _state.update { it.copy(message = text.take(SupportNewTicketUiState.MESSAGE_MAX_LENGTH)) }
    }

    override fun onSendClick() {
        val current = _state.value
        if (!current.canSend || created) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            when (val result = repository.createTicket(current.subject.trim(), current.message.trim())) {
                is DataResult.Success -> {
                    // Кнопка остаётся неактивной: экран сейчас закроется.
                    created = true
                    _createdTicketId.send(result.data.id)
                }

                is DataResult.Failure -> {
                    _state.update { it.copy(isSending = false) }
                    _messages.send(result.error.messageRes())
                }
            }
        }
    }
}