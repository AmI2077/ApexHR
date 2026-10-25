package com.example.apexhr.features.activities.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError

data class ActivitiesUiState(
    val searchQuery: String,
    val selectedCategoryId: String,
    val categories: List<ActivityCategoryUiModel>,
    val content: ActivitiesContentState,
) {
    companion object {
        const val CATEGORY_SURVEYS = "surveys"
        const val CATEGORY_MINI_GAMES = "mini_games"
        const val CATEGORY_EVENTS = "events"

        val Categories = listOf(
            ActivityCategoryUiModel(CATEGORY_SURVEYS, R.string.activities_category_surveys),
            ActivityCategoryUiModel(CATEGORY_MINI_GAMES, R.string.activities_category_mini_games),
            ActivityCategoryUiModel(CATEGORY_EVENTS, R.string.activities_category_events),
        )

        val Initial = ActivitiesUiState(
            searchQuery = "",
            selectedCategoryId = CATEGORY_SURVEYS,
            categories = Categories,
            content = ActivitiesContentState.Loading,
        )

        val Preview = Initial.copy(
            content = ActivitiesContentState.Content(
                activities = listOf(
                    ActivityUiModel(
                        id = 1,
                        kind = ActivityKind.Survey,
                        title = "Сколько в тебе хайпа?",
                        subtitle = ActivitySubtitle.Questions(12),
                        reward = null,
                        statusRes = null,
                    ),
                    ActivityUiModel(
                        id = 2,
                        kind = ActivityKind.Survey,
                        title = "Пульс команды",
                        subtitle = ActivitySubtitle.Questions(3),
                        reward = null,
                        statusRes = R.string.activities_status_survey_answered,
                    ),
                ),
                isFiltered = false,
            ),
        )
    }
}

/** Что показано под вкладками: загрузка, ошибка или список (уже отфильтрованный поиском). */
sealed interface ActivitiesContentState {
    data object Loading : ActivitiesContentState
    data class Error(val error: DataError) : ActivitiesContentState

    /** @param isFiltered true, если список сужен поиском (для текста пустого списка). */
    data class Content(
        val activities: List<ActivityUiModel>,
        val isFiltered: Boolean,
    ) : ActivitiesContentState
}

data class ActivityCategoryUiModel(
    val id: String,
    @StringRes val titleRes: Int,
)

enum class ActivityKind { Survey, Quest, Event }

data class ActivityUiModel(
    val id: Long,
    val kind: ActivityKind,
    /** null — сервер не прислал название, показываем заглушку. */
    val title: String?,
    val subtitle: ActivitySubtitle?,
    /** Награда в апексах, уже отформатированная; null — не показываем (опросы, награды нет). */
    val reward: String?,
    /** Статус участия пользователя; null — пользователь ещё не участвовал. */
    @StringRes val statusRes: Int?,
)

/** Вторая строка карточки: у опроса — вопросы, у квеста — шаги, у мероприятия — когда и где. */
sealed interface ActivitySubtitle {
    data class Questions(val count: Int) : ActivitySubtitle
    data class Steps(val count: Int) : ActivitySubtitle
    data class EventTime(val startAt: String?, val location: String?) : ActivitySubtitle
}
