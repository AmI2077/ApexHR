package com.example.apexhr.features.activities.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.domain.map
import com.example.apexhr.core.ui.model.loadMoreStateOf
import com.example.apexhr.features.activities.domain.AchievementsRepository
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivitiesRepository.Companion.FIRST_PAGE
import com.example.apexhr.features.activities.domain.ActivityPage
import com.example.apexhr.features.activities.ui.models.ActivitiesContentState
import com.example.apexhr.features.activities.ui.models.ActivitiesScreenCallbacks
import com.example.apexhr.features.activities.ui.models.ActivitiesUiState
import com.example.apexhr.features.activities.ui.models.ActivityUiModel
import com.example.apexhr.features.activities.ui.models.filterByQuery
import com.example.apexhr.features.activities.ui.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Вкладка «Активности»: опросы, квесты и мероприятия.
 * Список выбранной вкладки перечитывается (с первой страницы) при каждом показе экрана и
 * выборе вкладки; следующие страницы догружаются, когда список докручен до конца.
 * Поиск фильтрует уже загруженный список по названию. Переходы на детали задаёт навигация.
 */
class ActivitiesViewModel(
    private val repository: ActivitiesRepository,
    private val achievementsRepository: AchievementsRepository,
) : ViewModel(), ActivitiesScreenCallbacks {

    private val _state = MutableStateFlow(ActivitiesUiState.Initial)
    val state: StateFlow<ActivitiesUiState> = _state.asStateFlow()

    /** Загруженные разделы выбранной вкладки по порядку показа; null — ещё не загружены или ошибка. */
    private var sections: Map<Source, Section>? = null
    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null
    private var isLoadingMore = false
    private var loadMoreError: DataError? = null

    override fun onScreenShown() = load()

    override fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        render()
    }

    override fun onCategoryClick(categoryId: String) {
        if (categoryId != _state.value.selectedCategoryId) {
            sections = null
            _state.update { it.copy(selectedCategoryId = categoryId) }
        }
        load()
    }

    override fun onActivityClick(activity: ActivityUiModel) = Unit

    override fun onParticipateClick(activity: ActivityUiModel) = Unit

    override fun onRetryClick() = load()

    /**
     * Следующая страница каждого раздела, у которого она есть («Все» — квесты, опросы и
     * мероприятия параллельно). Загруженное не теряется; если какой-то раздел не загрузился,
     * в футере — ошибка и «Повторить», повтор догружает только оставшиеся страницы.
     */
    override fun onLoadMore() {
        val current = sections ?: return
        if (isLoadingMore) return
        val pending = current.filterValues { it.hasNextPage }
        if (pending.isEmpty()) return
        isLoadingMore = true
        loadMoreError = null
        render()
        loadMoreJob = viewModelScope.launch {
            val results = coroutineScope {
                pending.map { (source, section) -> async { source to loadPage(source, section.page + 1) } }
                    .awaitAll()
            }
            isLoadingMore = false
            // Пока грузилось, список перечитали с первой страницы: эти страницы уже не к месту.
            // Футер снова станет «Доступно» и сам запросит следующую страницу нового списка.
            if (sections !== current) {
                render()
                return@launch
            }
            val updated = current.toMutableMap()
            results.forEach { (source, result) ->
                if (result is DataResult.Success) {
                    val old = updated.getValue(source)
                    updated[source] = Section(
                        items = (old.items + result.data.items).distinctBy { it.id },
                        page = result.data.page,
                        hasNextPage = result.data.hasNextPage,
                    )
                }
            }
            sections = updated
            loadMoreError = results.firstNotNullOfOrNull { (_, result) -> (result as? DataResult.Failure)?.error }
            render()
        }
    }

    /**
     * Первая страница разделов вкладки. Если список уже на экране, обновляем его без
     * полноэкранного лоадера и при ошибке оставляем как есть.
     * «Все», как в вебе: квесты, опросы, мероприятия, затем достижения; раздел, который
     * не загрузился, пропускается, ошибка — только если не загрузилось ничего.
     */
    private fun load() {
        loadJob?.cancel()
        loadMoreJob?.cancel()
        isLoadingMore = false
        loadMoreError = null
        val sources = sourcesOf(_state.value.selectedCategoryId)
        if (sections == null) {
            _state.update { it.copy(content = ActivitiesContentState.Loading) }
        } else {
            render()
        }
        loadJob = viewModelScope.launch {
            val results = coroutineScope {
                sources.map { source -> async { source to loadPage(source, FIRST_PAGE) } }.awaitAll()
            }
            val loaded = results.mapNotNull { (source, result) ->
                (result as? DataResult.Success)?.let { source to it.data.toSection() }
            }
            if (loaded.isNotEmpty()) {
                sections = loaded.toMap()
                render()
            } else if (sections == null) {
                val error = (results.first().second as DataResult.Failure).error
                _state.update { it.copy(content = ActivitiesContentState.Error(error)) }
            }
        }
    }

    private fun sourcesOf(categoryId: String): List<Source> = when (categoryId) {
        ActivitiesUiState.CATEGORY_QUESTS -> listOf(Source.Quests)
        ActivitiesUiState.CATEGORY_EVENTS -> listOf(Source.Events)
        ActivitiesUiState.CATEGORY_ACHIEVEMENTS -> listOf(Source.Achievements)
        ActivitiesUiState.CATEGORY_ALL -> listOf(Source.Quests, Source.Surveys, Source.Events, Source.Achievements)
        else -> listOf(Source.Surveys)
    }

    /** Достижения сервер отдаёт одним списком, без страниц. */
    private suspend fun loadPage(source: Source, page: Int): DataResult<ActivityPage<ActivityUiModel>> =
        when (source) {
            Source.Quests -> repository.getQuests(page).map { it.mapItems { quest -> quest.toUiModel() } }
            Source.Surveys -> repository.getSurveys(page).map { it.mapItems { survey -> survey.toUiModel() } }
            Source.Events -> repository.getEvents(page).map { it.mapItems { event -> event.toUiModel() } }
            Source.Achievements -> achievementsRepository.getAchievements().map { items ->
                ActivityPage(items = items.map { it.toUiModel() }, page = FIRST_PAGE, hasNextPage = false)
            }
        }

    private fun render() {
        val loaded = sections ?: return
        _state.update { state ->
            state.copy(
                content = ActivitiesContentState.Content(
                    activities = loaded.values.flatMap { it.items }.filterByQuery(state.searchQuery),
                    isFiltered = state.searchQuery.isNotBlank(),
                    loadMore = loadMoreStateOf(
                        hasMore = loaded.values.any { it.hasNextPage },
                        isLoading = isLoadingMore,
                        error = loadMoreError,
                    ),
                ),
            )
        }
    }

    private enum class Source { Quests, Surveys, Events, Achievements }

    /** Загруженные страницы одного раздела. @param page последняя загруженная страница. */
    private data class Section(
        val items: List<ActivityUiModel>,
        val page: Int,
        val hasNextPage: Boolean,
    )

    private fun ActivityPage<ActivityUiModel>.toSection() = Section(items, page, hasNextPage)

    private inline fun <T> ActivityPage<T>.mapItems(map: (T) -> ActivityUiModel) =
        ActivityPage(items = items.map(map), page = page, hasNextPage = hasNextPage)
}
