package com.example.apexhr.features.activities.ui.quest.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError

data class QuestDetailsUiModel(
    val title: String?,
    val description: String?,
    val reward: String?,
    /** Дата окончания или null. */
    val endAt: String?,
    @StringRes val statusRes: Int?,
    val completedStepsCount: Int,
    val steps: List<QuestStepUiModel>,
    /** Действие с квестом целиком; null — кнопки нет, см. [noteRes]. */
    val action: QuestAction?,
    @StringRes val noteRes: Int?,
) {
    companion object {
        val Preview = QuestDetailsUiModel(
            title = "Тестовый квест",
            description = "Пройдите шаги и получите апексы",
            reward = "300",
            endAt = "31.10.2026 01:46",
            statusRes = R.string.activities_status_quest_in_progress,
            completedStepsCount = 1,
            steps = listOf(
                QuestStepUiModel(
                    id = 1,
                    title = "Прочитать правила",
                    description = null,
                    isRequired = true,
                    isCompleted = true,
                    canComplete = false,
                    stateRes = R.string.quest_step_completed,
                ),
                QuestStepUiModel(
                    id = 2,
                    title = "Загрузить фото",
                    description = "Фото с мероприятия",
                    isRequired = true,
                    isCompleted = false,
                    canComplete = false,
                    stateRes = R.string.quest_step_proof_unavailable,
                ),
            ),
            action = null,
            noteRes = R.string.quest_note_required_steps,
        )
    }
}

/**
 * @param canComplete шаг можно отметить выполненным (ручной, участник, ещё не выполнен).
 * @param stateRes состояние шага: выполнен, ждёт проверки, засчитается автоматически и т. п.
 */
data class QuestStepUiModel(
    val id: Long,
    val title: String?,
    val description: String?,
    val isRequired: Boolean,
    val isCompleted: Boolean,
    val canComplete: Boolean,
    @StringRes val stateRes: Int?,
)

sealed interface QuestAction {
    data object Join : QuestAction
    data class CompleteStep(val stepId: Long) : QuestAction

    /** Завершить квест и получить награду. */
    data object Complete : QuestAction
}

sealed interface QuestDetailsScreenState {
    data object Loading : QuestDetailsScreenState
    data class Error(val error: DataError) : QuestDetailsScreenState

    /** @param pendingAction действие, запрос которого идёт: все кнопки неактивны. */
    data class Content(
        val quest: QuestDetailsUiModel,
        val pendingAction: QuestAction?,
    ) : QuestDetailsScreenState
}

interface QuestDetailsScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()
    fun onActionClick(action: QuestAction)

    companion object {
        val Empty = object : QuestDetailsScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onActionClick(action: QuestAction) = Unit
        }
    }
}
