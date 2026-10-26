package com.example.apexhr.features.profile.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.domain.TransactionFilter
import com.example.apexhr.features.profile.domain.TransactionHistoryRepository
import com.example.apexhr.features.profile.ui.history.models.LoadedTransactions
import com.example.apexhr.features.profile.ui.history.models.TransactionHistoryScreenCallbacks
import com.example.apexhr.features.profile.ui.history.models.TransactionHistoryScreenState
import com.example.apexhr.features.profile.ui.history.models.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Полная история операций: первая страница при открытии, следующие — по
 * «Показать ещё». Фильтр применяется к уже загруженным операциям.
 * Назад обрабатывает навигация (onBackClick переопределяется в AppNavigation).
 */
class TransactionHistoryViewModel(
    private val repository: TransactionHistoryRepository,
) : ViewModel(), TransactionHistoryScreenCallbacks {

    private val _state =
        MutableStateFlow<TransactionHistoryScreenState>(TransactionHistoryScreenState.Loading)
    val state: StateFlow<TransactionHistoryScreenState> = _state.asStateFlow()

    private var loaded = LoadedTransactions.Empty
    private var filter = TransactionFilter.All
    private var isLoadingMore = false
    private var loadMoreError: DataError? = null
    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loaded = LoadedTransactions.Empty
        isLoadingMore = false
        loadMoreError = null
        _state.value = TransactionHistoryScreenState.Loading
        loadJob = viewModelScope.launch {
            when (val result = repository.loadPage(skip = 0)) {
                is DataResult.Success -> {
                    loaded += result.data
                    publish()
                }
                is DataResult.Failure ->
                    _state.value = TransactionHistoryScreenState.Error(result.error)
            }
        }
    }

    override fun onRetryClick() = load()

    override fun onLoadMoreClick() {
        if (_state.value !is TransactionHistoryScreenState.Content) return
        if (isLoadingMore || !loaded.hasMore) return
        isLoadingMore = true
        loadMoreError = null
        publish()
        loadJob = viewModelScope.launch {
            val result = repository.loadPage(skip = loaded.nextSkip)
            isLoadingMore = false
            when (result) {
                is DataResult.Success -> loaded += result.data
                is DataResult.Failure -> loadMoreError = result.error
            }
            publish()
        }
    }

    override fun onFilterClick(filter: TransactionFilter) {
        this.filter = filter
        if (_state.value is TransactionHistoryScreenState.Content) publish()
    }

    override fun onBackClick() = Unit

    private fun publish() {
        _state.value = TransactionHistoryScreenState.Content(
            loaded.toUiState(
                filter = filter,
                isLoadingMore = isLoadingMore,
                loadMoreError = loadMoreError,
            )
        )
    }
}
