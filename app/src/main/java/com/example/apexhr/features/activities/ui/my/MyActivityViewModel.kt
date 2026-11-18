package com.example.apexhr.features.activities.ui.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.domain.AchievementsRepository
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.ui.my.models.MyActivityScreenCallbacks
import com.example.apexhr.features.activities.ui.my.models.MyActivityScreenState
import com.example.apexhr.features.activities.ui.my.models.buildMyActivityUiState
import com.example.apexhr.features.engagement.domain.EngagementRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * «Моя активность». Как в вебе, берётся первая страница списков (по 50) и
 * каталог достижений. Раздел, который не загрузился, показывается пустым;
 * ошибка экрана — только если не загрузилось ни одного раздела. Уровень
 * необязательный: без него карточка показывает текст ошибки.
 */
class MyActivityViewModel(
    private val activitiesRepository: ActivitiesRepository,
    private val achievementsRepository: AchievementsRepository,
    private val engagementRepository: EngagementRepository,
) : ViewModel(), MyActivityScreenCallbacks {

    private val _state = MutableStateFlow<MyActivityScreenState>(MyActivityScreenState.Loading)
    val state: StateFlow<MyActivityScreenState> = _state.asStateFlow()

    private var loadJob: Job? = null

    override fun onScreenShown() = load()

    override fun onRetryClick() = load()

    /** Если данные уже на экране, обновляем без лоадера и при ошибке оставляем их. */
    private fun load() {
        loadJob?.cancel()
        if (_state.value !is MyActivityScreenState.Content) {
            _state.value = MyActivityScreenState.Loading
        }
        loadJob = viewModelScope.launch {
            coroutineScope {
                val level = async { engagementRepository.getActivityLevel() }
                val quests = async { activitiesRepository.getQuests() }
                val surveys = async { activitiesRepository.getSurveys() }
                val events = async { activitiesRepository.getEvents() }
                val achievements = async { achievementsRepository.getAchievements() }
                val sections = listOf(quests.await(), surveys.await(), events.await(), achievements.await())
                if (sections.all { it is DataResult.Failure }) {
                    if (_state.value !is MyActivityScreenState.Content) {
                        _state.value = MyActivityScreenState.Error((sections.first() as DataResult.Failure).error)
                    }
                    return@coroutineScope
                }
                _state.value = MyActivityScreenState.Content(
                    buildMyActivityUiState(
                        level = (level.await() as? DataResult.Success)?.data,
                        quests = (quests.await() as? DataResult.Success)?.data?.items.orEmpty(),
                        surveys = (surveys.await() as? DataResult.Success)?.data?.items.orEmpty(),
                        events = (events.await() as? DataResult.Success)?.data?.items.orEmpty(),
                        achievements = (achievements.await() as? DataResult.Success)?.data.orEmpty(),
                    ),
                )
            }
        }
    }
}
