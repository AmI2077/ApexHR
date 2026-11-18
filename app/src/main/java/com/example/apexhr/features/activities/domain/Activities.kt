package com.example.apexhr.features.activities.domain

import java.time.Instant

// Анкеты

data class Survey(
    val id: Long,
    val title: String?,
    val description: String?,
    val questionsCount: Int,
    val hasResponded: Boolean,
    val canRespond: Boolean,
)

data class SurveyDetails(
    val id: Long,
    val title: String?,
    val description: String?,
    /** Можно ли сейчас отправить ответ (с учётом дат и правила повторных ответов). */
    val canRespond: Boolean,
    val allowMultipleResponses: Boolean,
    /** Вопросы в порядке sortOrder. */
    val questions: List<SurveyQuestion>,
    /** Время последнего отправленного ответа; null — пользователь ещё не отвечал. */
    val lastSubmittedAt: Instant?,
    val hasResponded: Boolean,
)

data class SurveyQuestion(
    val id: Long,
    val text: String?,
    val type: SurveyQuestionType,
    val options: List<String>,
    val isRequired: Boolean,
)

enum class SurveyQuestionType {
    Text,
    SingleChoice,
    MultipleChoice,
    Number,
    Boolean,
    Rating,

    /** Тип, которого клиент не знает: ответить на такой вопрос нельзя. */
    Unknown,
}

/** Ответ на один вопрос анкеты; формат значения на сервере зависит от типа. */
sealed interface SurveyAnswer {
    data class Text(val value: String) : SurveyAnswer
    data class SingleChoice(val option: String) : SurveyAnswer
    data class MultipleChoice(val options: List<String>) : SurveyAnswer
    data class Number(val value: Double) : SurveyAnswer
    data class Bool(val value: Boolean) : SurveyAnswer
    data class Rating(val value: Int) : SurveyAnswer
}

// Квесты

data class Quest(
    val id: Long,
    val title: String?,
    val description: String?,
    val rewardAmount: Long,
    val stepsCount: Int,
    val participationStatus: QuestParticipationStatus?,
)

data class QuestDetails(
    val id: Long,
    val title: String?,
    val description: String?,
    val rewardAmount: Long,
    val isActive: Boolean,
    val endAt: Instant?,
    /** null — пользователь ещё не присоединился. */
    val participation: QuestParticipation?,
    /** Шаги в порядке sortOrder. */
    val steps: List<QuestStep>,
)

data class QuestParticipation(
    val status: QuestParticipationStatus,
    val rewardClaimed: Boolean,
)

enum class QuestParticipationStatus { InProgress, Completed, Cancelled, Unknown }

data class QuestStep(
    val id: Long,
    val title: String?,
    val description: String?,
    val completionType: QuestStepType,
    val isRequired: Boolean,
    val isCompleted: Boolean,
    val proofStatus: ProofStatus?,
    /** Публичная ссылка на отправленный файл подтверждения. */
    val proofStoragePath: String? = null,
    val proofComment: String? = null,
)

enum class QuestStepType {
    /** Пользователь отмечает сам. */
    Manual,

    /** Нужно подтверждение: файл в Storage и необязательный комментарий. */
    Proof,

    /** Засчитывается системой; сервер пока не поддерживает. */
    Automatic,
    Unknown,
}

enum class ProofStatus { Pending, Approved, Rejected, Unknown }

// Мероприятия

data class Event(
    val id: Long,
    val title: String?,
    val description: String?,
    val startAt: Instant?,
    val location: String?,
    val rewardAmount: Long,
    val registrationStatus: EventRegistrationStatus?,
)

data class EventDetails(
    val id: Long,
    val title: String?,
    val description: String?,
    val startAt: Instant?,
    val endAt: Instant?,
    val location: String?,
    val maxParticipants: Int?,
    val registeredCount: Int,
    val rewardAmount: Long,
    /** Открыта ли регистрация. */
    val isOpen: Boolean,
    /** null — пользователь не регистрировался. */
    val registration: EventRegistration?,
)

data class EventRegistration(
    val status: EventRegistrationStatus,
    val rewardClaimed: Boolean,
)

enum class EventRegistrationStatus { Registered, Attended, Cancelled, NoShow, Unknown }
