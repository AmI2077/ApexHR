package com.example.apexhr.features.activities.ui.my.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.activities.domain.Achievement
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.engagement.domain.ActivityLevel
import com.example.apexhr.features.engagement.ui.ActivityLevelUiModel
import com.example.apexhr.features.engagement.ui.toUiModel

sealed interface MyActivityScreenState {
    data object Loading : MyActivityScreenState
    data class Error(val error: DataError) : MyActivityScreenState
    data class Content(val uiState: MyActivityUiState) : MyActivityScreenState
}

/**
 * «Моя активность», как /my-activity в вебе: уровень с разбивкой очков и то,
 * в чём пользователь уже участвует.
 * @param level null — уровень не загрузился.
 */
data class MyActivityUiState(
    val level: ActivityLevelUiModel?,
    val quests: List<MyActivityRowUiModel>,
    val surveys: List<MyActivityRowUiModel>,
    val events: List<MyActivityRowUiModel>,
    val achievements: List<MyAchievementUiModel>,
) {
    /** Ни в чём не участвует: как в вебе, ссылка на все активности. */
    val isEmpty: Boolean
        get() = quests.isEmpty() && surveys.isEmpty() && events.isEmpty() && achievements.isEmpty()

    companion object {
        val Preview = MyActivityUiState(
            level = ActivityLevelUiModel.Preview,
            quests = listOf(MyActivityRowUiModel(2, "Неделя в команде", R.string.my_activity_quest_in_progress, 50)),
            surveys = listOf(MyActivityRowUiModel(2, "Пульс команды", R.string.my_activity_survey_answered, 50)),
            events = emptyList(),
            achievements = listOf(MyAchievementUiModel(1, "Первая благодарность", "Поблагодарите коллегу")),
        )
    }
}

/**
 * Квест, опрос или мероприятие с участием пользователя.
 * @param title null — сервер не прислал название.
 * @param statusRes «В процессе», «Ответ отправлен», «Вы записаны»…
 * @param reward награда в апексах; null — не показываем.
 */
data class MyActivityRowUiModel(
    val id: Long,
    val title: String?,
    @StringRes val statusRes: Int,
    val reward: Long?,
)

data class MyAchievementUiModel(
    val id: Long,
    val title: String?,
    val description: String?,
)

/**
 * Отбор как в вебе: квесты в процессе и завершённые, опросы с ответом,
 * мероприятия с записью или посещением, полученные достижения.
 */
fun buildMyActivityUiState(
    level: ActivityLevel?,
    quests: List<Quest>,
    surveys: List<Survey>,
    events: List<Event>,
    achievements: List<Achievement>,
): MyActivityUiState = MyActivityUiState(
    level = level?.toUiModel(),
    quests = quests.mapNotNull { quest ->
        val statusRes = when (quest.participationStatus) {
            QuestParticipationStatus.InProgress -> R.string.my_activity_quest_in_progress
            QuestParticipationStatus.Completed -> R.string.my_activity_quest_completed
            else -> return@mapNotNull null
        }
        MyActivityRowUiModel(quest.id, quest.title, statusRes, quest.rewardAmount)
    },
    surveys = surveys.filter { it.hasResponded }.map { survey ->
        MyActivityRowUiModel(
            id = survey.id,
            title = survey.title,
            statusRes = R.string.my_activity_survey_answered,
            reward = survey.rewardAmount.takeIf { it > 0 },
        )
    },
    events = events.mapNotNull { event ->
        val statusRes = when (event.registrationStatus) {
            EventRegistrationStatus.Registered -> R.string.my_activity_event_registered
            EventRegistrationStatus.Attended -> R.string.my_activity_event_attended
            else -> return@mapNotNull null
        }
        // Как в вебе: у мероприятия в «Моей активности» награда не показывается.
        MyActivityRowUiModel(event.id, event.title, statusRes, reward = null)
    },
    achievements = achievements.filter { it.isEarned }.map {
        MyAchievementUiModel(id = it.id, title = it.name, description = it.description)
    },
)

interface MyActivityScreenCallbacks {
    /** Экран показан (вход или возврат с деталей): данные перечитываются. */
    fun onScreenShown()
    fun onRetryClick()
    fun onBackClick() = Unit
    fun onQuestClick(questId: Long) = Unit
    fun onSurveyClick(surveyId: Long) = Unit
    fun onEventClick(eventId: Long) = Unit
    fun onAllActivitiesClick() = Unit

    companion object {
        val Empty = object : MyActivityScreenCallbacks {
            override fun onScreenShown() = Unit
            override fun onRetryClick() = Unit
        }
    }
}
