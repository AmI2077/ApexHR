package com.example.apexhr.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.home.domain.HomeRepository
import com.example.apexhr.features.home.ui.models.HomeScreenCallbacks
import com.example.apexhr.features.home.ui.models.HomeScreenState
import com.example.apexhr.features.home.ui.models.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository,
) : ViewModel(), HomeScreenCallbacks {

    private val _state = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = HomeScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = repository.loadHome()) {
                is DataResult.Success -> HomeScreenState.Content(result.data.toUiState())
                is DataResult.Failure -> HomeScreenState.Error(result.error)
            }
        }
    }

    override fun onRetryClick() = load()

    // Экранов баланса, квеста, пользователя и достижения пока нет.
    override fun onPointsClick() = Unit
    override fun onChallengeClick(challengeId: String) = Unit
    override fun onLeaderboardEntryClick(entryId: String) = Unit
    override fun onAchievementClick(achievementId: String) = Unit
}
