package com.example.apexhr.features.news.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.news.domain.NewsRepository
import com.example.apexhr.features.news.ui.models.NewsDetailsScreenCallbacks
import com.example.apexhr.features.news.ui.models.NewsDetailsScreenState
import com.example.apexhr.features.news.ui.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Новость целиком: GET /api/company-news/{id}. */
class NewsDetailsViewModel(
    private val newsId: Long,
    private val repository: NewsRepository,
) : ViewModel(), NewsDetailsScreenCallbacks {

    private val _state = MutableStateFlow<NewsDetailsScreenState>(NewsDetailsScreenState.Loading)
    val state: StateFlow<NewsDetailsScreenState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = NewsDetailsScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = repository.getNewsDetails(newsId)) {
                is DataResult.Success -> NewsDetailsScreenState.Content(result.data.toUiModel())
                is DataResult.Failure -> NewsDetailsScreenState.Error(result.error)
            }
        }
    }

    override fun onRetryClick() = load()
}
