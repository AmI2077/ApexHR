package com.example.apexhr.features.support.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.support.domain.SupportRepository
import com.example.apexhr.features.support.ui.models.SupportListScreenCallbacks
import com.example.apexhr.features.support.ui.models.SupportListScreenState
import com.example.apexhr.features.support.ui.models.SupportListUiState
import com.example.apexhr.features.support.ui.models.appendPage
import com.example.apexhr.features.support.ui.models.toUiModel
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
 * «Мои обращения». Список загружает [refresh]: экран вызывает его каждый раз,
 * когда снова виден, чтобы после создания обращения или ответа поддержки
 * список был свежим. Повторная загрузка идёт без индикатора и при ошибке
 * оставляет прежний список. Переходы задаёт навигация.
 */
class SupportListViewModel(
    private val repository: SupportRepository,
) : ViewModel(), SupportListScreenCallbacks {

    private val _state = MutableStateFlow<SupportListScreenState>(SupportListScreenState.Loading)
    val state: StateFlow<SupportListScreenState> = _state.asStateFlow()

    private val _messages = Channel<Int>(Channel.BUFFERED)

    /** Разовые сообщения об ошибках (id строки). */
    val messages: Flow<Int> = _messages.receiveAsFlow()

    private var loadJob: Job? = null
    private var loadedPage = 0

    fun refresh() {
        val silent = _state.value is SupportListScreenState.Content
        if (!silent) _state.value = SupportListScreenState.Loading
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val result = repository.getTickets(page = 1)) {
                is DataResult.Success -> {
                    loadedPage = result.data.page.coerceAtLeast(1)
                    _state.value = SupportListScreenState.Content(
                        SupportListUiState(
                            items = result.data.items.map { it.toUiModel() },
                            hasNextPage = result.data.hasNextPage,
                        )
                    )
                }

                is DataResult.Failure -> if (!silent) {
                    _state.value = SupportListScreenState.Error(result.error)
                }
            }
        }
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = refresh()

    override fun onLoadMoreClick() {
        val current = content() ?: return
        if (!current.hasNextPage || current.isLoadingMore) return
        updateContent { it.copy(isLoadingMore = true) }
        loadJob = viewModelScope.launch {
            when (val result = repository.getTickets(page = loadedPage + 1)) {
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

    private fun content(): SupportListUiState? =
        (_state.value as? SupportListScreenState.Content)?.uiState

    private fun updateContent(transform: (SupportListUiState) -> SupportListUiState) {
        _state.update { state ->
            if (state is SupportListScreenState.Content) SupportListScreenState.Content(transform(state.uiState)) else state
        }
    }
}