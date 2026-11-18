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
        /** «Все»: квесты, опросы, мероприятия и достижения одним списком, как в вебе. */
        const val CATEGORY_ALL = "all"
        const val CATEGORY_SURVEYS = "surveys"
        const val CATEGORY_QUESTS = "quests"
        const val CATEGORY_EVENTS = "events"
        const val CATEGORY_ACHIEVEMENTS = "achievements"

        val Categories = listOf(
            ActivityCategoryUiModel(CATEGORY_ALL, R.string.activities_category_all),
            ActivityCategoryUiModel(CATEGORY_SURVEYS, R.string.activities_category_surveys),
            ActivityCategoryUiModel(CATEGORY_QUESTS, R.string.activities_category_quests),
            ActivityCategoryUiModel(CATEGORY_EVENTS, R.string.activities_category_events),
            ActivityCategoryUiModel(CATEGORY_ACHIEVEMENTS, R.string.activities_category_achievements),
        )

        /** Как в вебе, по умолчанию открыта вкладка «Все». */
        val Initial = ActivitiesUiState(
            searchQuery = "",
            selectedCategoryId = CATEGORY_ALL,
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

enum class ActivityKind { Survey, Quest, Event, Achievement }

data class ActivityUiModel(
    val id: Long,
    val kind: ActivityKind,
    /** null — сервер не прислал название, показываем заглушку. */
    val title: String?,
    val subtitle: ActivitySubtitle?,
    /** Награда в апексах (рисуется через ApexAmount); null — не показываем (опросы, награды нет). */
    val reward: Long?,
    /** Статус участия пользователя; null — пользователь ещё не участвовал. */
    @StringRes val statusRes: Int?,
    /** Показывать кнопку «Участвовать»; у достижений её нет. */
    val canParticipate: Boolean = true,
)

/** Вторая строка карточки: у опроса — вопросы, у квеста — шаги, у мероприятия — когда и где. */
sealed interface ActivitySubtitle {
    data class Questions(val count: Int) : ActivitySubtitle
    data class Steps(val count: Int) : ActivitySubtitle
    data class EventTime(val startAt: String?, val location: String?) : ActivitySubtitle

    /**
     * Достижение: описание и, как в вебе, «Открыто <дата>» у полученного
     * или условие получения у ещё не полученного.
     * @param earnedAt дата получения; null у неполученного или если сервер не прислал дату.
     * @param conditionRes подсказка условия; null — достижение уже получено.
     */
    data class AchievementInfo(
        val description: String?,
        val isEarned: Boolean,
        val earnedAt: String?,
        @StringRes val conditionRes: Int?,
    ) : ActivitySubtitle
}
