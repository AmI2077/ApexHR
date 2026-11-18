package com.example.apexhr.features.news.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.news.domain.NewsRepository
import com.example.apexhr.features.news.ui.models.NewsScreenCallbacks
import com.example.apexhr.features.news.ui.models.NewsScreenState
import com.example.apexhr.features.news.ui.models.NewsUiState
import com.example.apexhr.features.news.ui.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Лента новостей: первая страница при открытии, следующие — у конца списка. */
class NewsViewModel(
    private val repository: NewsRepository,
) : ViewModel(), NewsScreenCallbacks {

    private val _state = MutableStateFlow<NewsScreenState>(NewsScreenState.Loading)
    val state: StateFlow<NewsScreenState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var loadedPage = 0

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = NewsScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = repository.getNews(page = 1)) {
                is DataResult.Success -> {
                    loadedPage = 1
                    NewsScreenState.Content(
                        NewsUiState(
                            items = result.data.items.map { it.toUiModel() },
                            hasNextPage = result.data.hasNextPage,
                        ),
                    )
                }

                is DataResult.Failure -> NewsScreenState.Error(result.error)
            }
        }
    }

    override fun onRetryClick() = load()

    override fun onLoadMore() {
        val current = (_state.value as? NewsScreenState.Content)?.uiState ?: return
        if (!current.hasNextPage || current.isLoadingMore) return
        updateContent { it.copy(isLoadingMore = true, loadMoreError = null) }
        loadJob = viewModelScope.launch {
            when (val result = repository.getNews(page = loadedPage + 1)) {
                is DataResult.Success -> {
                    loadedPage += 1
                    updateContent {
                        val known = it.items.map { item -> item.id }.toSet()
                        it.copy(
                            items = it.items + result.data.items.filter { news -> news.id !in known }.map { news -> news.toUiModel() },
                            hasNextPage = result.data.hasNextPage,
                            isLoadingMore = false,
                        )
                    }
                }

                // Ошибка показывается в конце списка с кнопкой «Повторить».
                is DataResult.Failure -> updateContent {
                    it.copy(isLoadingMore = false, loadMoreError = result.error)
                }
            }
        }
    }

    private fun updateContent(transform: (NewsUiState) -> NewsUiState) {
        _state.update { state ->
            if (state is NewsScreenState.Content) NewsScreenState.Content(transform(state.uiState)) else state
        }
    }
}
