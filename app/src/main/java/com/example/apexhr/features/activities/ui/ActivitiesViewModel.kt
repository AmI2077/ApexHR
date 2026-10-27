package com.example.apexhr.features.activities.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.domain.map
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.ui.models.ActivitiesContentState
import com.example.apexhr.features.activities.ui.models.ActivitiesScreenCallbacks
import com.example.apexhr.features.activities.ui.models.ActivitiesUiState
import com.example.apexhr.features.activities.ui.models.ActivityUiModel
import com.example.apexhr.features.activities.ui.models.filterByQuery
import com.example.apexhr.features.activities.ui.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Вкладка «Активности»: опросы, квесты и мероприятия.
 * Список выбранной вкладки перечитывается при каждом показе экрана и выборе вкладки;
 * поиск фильтрует уже загруженный список по названию. Переходы на детали задаёт навигация.
 */
class ActivitiesViewModel(
    private val repository: ActivitiesRepository,
) : ViewModel(), ActivitiesScreenCallbacks {

    private val _state = MutableStateFlow(ActivitiesUiState.Initial)
    val state: StateFlow<ActivitiesUiState> = _state.asStateFlow()

    /** Загруженный список выбранной вкладки; null — ещё не загружен или ошибка. */
    private var loaded: List<ActivityUiModel>? = null
    private var loadJob: Job? = null

    override fun onScreenShown() = load()

    override fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        render()
    }

    override fun onCategoryClick(categoryId: String) {
        if (categoryId != _state.value.selectedCategoryId) {
            loaded = null
            _state.update { it.copy(selectedCategoryId = categoryId) }
        }
        load()
    }

    override fun onActivityClick(activity: ActivityUiModel) = Unit

    override fun onParticipateClick(activity: ActivityUiModel) = Unit

    override fun onRetryClick() = load()

    /** Если список уже на экране, обновляем его без полноэкранного лоадера. */
    private fun load() {
        loadJob?.cancel()
        val categoryId = _state.value.selectedCategoryId
        if (loaded == null) {
            _state.update { it.copy(content = ActivitiesContentState.Loading) }
        }
        loadJob = viewModelScope.launch {
            when (val result = loadCategory(categoryId)) {
                is DataResult.Success -> {
                    loaded = result.data
                    render()
                }

                is DataResult.Failure -> if (loaded == null) {
                    _state.update { it.copy(content = ActivitiesContentState.Error(result.error)) }
                }
            }
        }
    }

    private suspend fun loadCategory(categoryId: String): DataResult<List<ActivityUiModel>> =
        when (categoryId) {
            ActivitiesUiState.CATEGORY_QUESTS ->
                repository.getQuests().map { quests -> quests.map { it.toUiModel() } }

            ActivitiesUiState.CATEGORY_EVENTS ->
                repository.getEvents().map { events -> events.map { it.toUiModel() } }

            else -> repository.getSurveys().map { surveys -> surveys.map { it.toUiModel() } }
        }

    private fun render() {
        val activities = loaded ?: return
        _state.update { state ->
            state.copy(
                content = ActivitiesContentState.Content(
                    activities = activities.filterByQuery(state.searchQuery),
                    isFiltered = state.searchQuery.isNotBlank(),
                ),
            )
        }
    }
}
