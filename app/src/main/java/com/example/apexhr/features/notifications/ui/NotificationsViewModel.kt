package com.example.apexhr.features.notifications.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.notifications.domain.NotificationsRepository
import com.example.apexhr.features.notifications.ui.models.NotificationsScreenCallbacks
import com.example.apexhr.features.notifications.ui.models.NotificationsScreenState
import com.example.apexhr.features.notifications.ui.models.NotificationsUiState
import com.example.apexhr.features.notifications.ui.models.appendPage
import com.example.apexhr.features.notifications.ui.models.markAllRead
import com.example.apexhr.features.notifications.ui.models.markRead
import com.example.apexhr.features.notifications.ui.models.markUnread
import com.example.apexhr.features.notifications.ui.models.toUiModel
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
 * Уведомления: первая страница при открытии, «Показать ещё», отметка одного
 * уведомления по нажатию и «Прочитать все». Отметка применяется сразу и
 * откатывается, если сервер её не принял. «Назад» задаёт навигация.
 */
class NotificationsViewModel(
    private val repository: NotificationsRepository,
) : ViewModel(), NotificationsScreenCallbacks {

    private val _state = MutableStateFlow<NotificationsScreenState>(NotificationsScreenState.Loading)
    val state: StateFlow<NotificationsScreenState> = _state.asStateFlow()

    private val _messages = Channel<Int>(Channel.BUFFERED)

    /** Разовые сообщения об ошибках действий (id строки). */
    val messages: Flow<Int> = _messages.receiveAsFlow()

    private var loadJob: Job? = null
    private var loadedPage = 0

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = NotificationsScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = repository.getNotifications(page = 1)) {
                is DataResult.Success -> {
                    loadedPage = result.data.page.coerceAtLeast(1)
                    NotificationsScreenState.Content(
                        NotificationsUiState(
                            items = result.data.items.map { it.toUiModel() },
                            hasNextPage = result.data.hasNextPage,
                        )
                    )
                }

                is DataResult.Failure -> NotificationsScreenState.Error(result.error)
            }
        }
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()

    override fun onLoadMoreClick() {
        val current = content() ?: return
        if (!current.hasNextPage || current.isLoadingMore) return
        updateContent { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.getNotifications(page = loadedPage + 1)) {
                is DataResult.Success -> {
                    loadedPage = result.data.page.coerceAtLeast(loadedPage + 1)
                    updateContent {
                        it.copy(
                            items = it.items.appendPage(result.data.items.map { item -> item.toUiModel() }),
                            hasNextPage = result.data.hasNextPage,
                            isLoadingMore = false,
                        )
                    }
                }

                is DataResult.Failure -> {
                    updateContent { it.copy(isLoadingMore = false) }
                    _messages.send(result.error.messageRes())
                }
            }
        }
    }

    override fun onNotificationClick(id: Long) {
        val current = content() ?: return
        val item = current.items.firstOrNull { it.id == id } ?: return
        if (item.isRead) return
        updateContent { it.copy(items = it.items.markRead(id)) }
        viewModelScope.launch {
            if (repository.markRead(id) is DataResult.Failure) {
                updateContent { it.copy(items = it.items.markUnread(id)) }
                _messages.send(R.string.notifications_error_read)
            }
        }
    }

    override fun onReadAllClick() {
        val current = content() ?: return
        if (!current.hasUnread || current.isMarkingAll) return
        updateContent { it.copy(isMarkingAll = true) }
        viewModelScope.launch {
            when (repository.markAllRead()) {
                is DataResult.Success -> updateContent {
                    it.copy(items = it.items.markAllRead(), isMarkingAll = false)
                }

                is DataResult.Failure -> {
                    updateContent { it.copy(isMarkingAll = false) }
                    _messages.send(R.string.notifications_error_read)
                }
            }
        }
    }

    private fun content(): NotificationsUiState? =
        (_state.value as? NotificationsScreenState.Content)?.uiState

    private fun updateContent(transform: (NotificationsUiState) -> NotificationsUiState) {
        _state.update { state ->
            if (state is NotificationsScreenState.Content) {
                NotificationsScreenState.Content(transform(state.uiState))
            } else {
                state
            }
        }
    }
}
