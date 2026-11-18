package com.example.apexhr.features.activities.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Ответы /api/surveys, /api/quests и /api/events. Все поля со значениями
// по умолчанию: бэкенд ещё меняется, а пустое поле не должно ронять экран.
// Квесты главной (features/home) описаны отдельно и здесь не используются.

/** Страница списка: `{items, totalCount, page, pageSize, totalPages, …}`. */
@Serializable
data class ActivityPageDto<T>(
    val items: List<T> = emptyList(),
    val totalCount: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 0,
    val hasNextPage: Boolean = false,
)

// Анкеты

@Serializable
data class SurveyListItemDto(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val allowMultipleResponses: Boolean = false,
    val questionsCount: Int = 0,
    val hasResponded: Boolean = false,
    val canRespond: Boolean = true,
    val lastSubmittedAt: String? = null,
)

@Serializable
data class SurveyDetailsDto(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val startAt: String? = null,
    val endAt: String? = null,
    val allowMultipleResponses: Boolean = false,
    val canRespond: Boolean = true,
    val questions: List<SurveyQuestionDto> = emptyList(),
    val responses: List<SurveyResponseDto> = emptyList(),
)

@Serializable
data class SurveyQuestionDto(
    val id: Long,
    val questionText: String? = null,
    /** text, single_choice, multiple_choice, number, boolean, rating. */
    val questionType: String? = null,
    val options: List<String>? = null,
    val isRequired: Boolean = false,
    val sortOrder: Int = 0,
)

/** Отправленный ранее ответ пользователя (в деталях анкеты и в my-responses). */
@Serializable
data class SurveyResponseDto(
    val id: Long = 0,
    val submittedAt: String? = null,
    val answers: List<SurveyAnswerDto> = emptyList(),
)

/** @param value строка, число, логическое значение или массив строк — в зависимости от типа вопроса. */
@Serializable
data class SurveyAnswerDto(
    val questionId: Long,
    val value: JsonElement? = null,
)

/** Тело POST /api/surveys/{id}/responses. */
@Serializable
data class SubmitSurveyRequest(
    val answers: List<SurveyAnswerDto>,
)

// Квесты

@Serializable
data class ActivityQuestListItemDto(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val rewardAmount: Long = 0,
    val startAt: String? = null,
    val endAt: String? = null,
    val stepsCount: Int = 0,
    val requiredStepsCount: Int = 0,
    /** in_progress, completed, cancelled; null — пользователь не участвует. */
    val participationStatus: String? = null,
)

@Serializable
data class QuestDetailsDto(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val rewardAmount: Long = 0,
    val isActive: Boolean = true,
    val startAt: String? = null,
    val endAt: String? = null,
    val participation: QuestParticipationDto? = null,
    val steps: List<QuestStepDto> = emptyList(),
)

@Serializable
data class QuestParticipationDto(
    val id: Long = 0,
    val status: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val rewardClaimed: Boolean = false,
)

@Serializable
data class QuestStepDto(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    /** manual, proof, automatic. */
    val completionType: String? = null,
    val sortOrder: Int = 0,
    val isRequired: Boolean = true,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    /** pending, approved, rejected — для шагов с доказательством. */
    val proofStatus: String? = null,
    /** Ссылка на уже отправленный файл подтверждения. */
    val proofStoragePath: String? = null,
    val proofComment: String? = null,
)

/**
 * Тело POST /api/quests/{id}/steps/{stepId}/complete (для ручного шага оба поля пустые)
 * и PUT /api/quests/{id}/steps/{stepId}/proof.
 */
@Serializable
data class CompleteQuestStepRequest(
    val storagePath: String? = null,
    val comment: String? = null,
)

// Мероприятия

@Serializable
data class EventListItemDto(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val location: String? = null,
    val maxParticipants: Int? = null,
    val registeredCount: Int = 0,
    val rewardAmount: Long = 0,
    /** registered, attended, cancelled, no_show; null — не регистрировался. */
    val registrationStatus: String? = null,
)

@Serializable
data class EventDetailsDto(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val location: String? = null,
    val maxParticipants: Int? = null,
    val registeredCount: Int = 0,
    val rewardAmount: Long = 0,
    val isActive: Boolean = true,
    val isOpen: Boolean = true,
    val registration: EventRegistrationDto? = null,
)

/** Регистрация текущего пользователя. Имя и аватар не используем: это сам пользователь. */
@Serializable
data class EventRegistrationDto(
    val id: Long = 0,
    val status: String? = null,
    val registeredAt: String? = null,
    val attendedAt: String? = null,
    val rewardClaimed: Boolean = false,
)
