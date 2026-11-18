package com.example.apexhr.features.colleague.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.colleague.domain.ColleagueRepository
import com.example.apexhr.features.colleague.ui.models.ColleagueProfileScreenCallbacks
import com.example.apexhr.features.colleague.ui.models.ColleagueProfileScreenState
import com.example.apexhr.features.colleague.ui.models.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Профиль коллеги по id. Переходы («Назад», «Поблагодарить», свой профиль) задаёт навигация. */
class ColleagueProfileViewModel(
    private val userId: String,
    private val repository: ColleagueRepository,
) : ViewModel(), ColleagueProfileScreenCallbacks {

    private val _state = MutableStateFlow<ColleagueProfileScreenState>(ColleagueProfileScreenState.Loading)
    val state: StateFlow<ColleagueProfileScreenState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = ColleagueProfileScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = repository.loadColleague(userId)) {
                is DataResult.Success -> ColleagueProfileScreenState.Content(result.data.toUiState())
                is DataResult.Failure -> ColleagueProfileScreenState.Error(result.error)
            }
        }
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()
}