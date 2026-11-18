package com.example.apexhr.features.activities.data

import com.example.apexhr.features.activities.data.dto.ActivityQuestListItemDto
import com.example.apexhr.features.activities.data.dto.EventDetailsDto
import com.example.apexhr.features.activities.data.dto.EventListItemDto
import com.example.apexhr.features.activities.data.dto.EventRegistrationDto
import com.example.apexhr.features.activities.data.dto.QuestDetailsDto
import com.example.apexhr.features.activities.data.dto.QuestParticipationDto
import com.example.apexhr.features.activities.data.dto.QuestStepDto
import com.example.apexhr.features.activities.data.dto.SurveyAnswerDto
import com.example.apexhr.features.activities.data.dto.SurveyDetailsDto
import com.example.apexhr.features.activities.data.dto.SurveyListItemDto
import com.example.apexhr.features.activities.data.dto.SurveyQuestionDto
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventDetails
import com.example.apexhr.features.activities.domain.EventRegistration
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.domain.ProofStatus
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.QuestParticipation
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.QuestStep
import com.example.apexhr.features.activities.domain.QuestStepType
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.activities.domain.SurveyAnswer
import com.example.apexhr.features.activities.domain.SurveyDetails
import com.example.apexhr.features.activities.domain.SurveyQuestion
import com.example.apexhr.features.activities.domain.SurveyQuestionType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

// Анкеты

fun SurveyListItemDto.toDomain(): Survey = Survey(
    id = id,
    title = title.cleaned(),
    description = description.cleaned(),
    questionsCount = questionsCount.coerceAtLeast(0),
    hasResponded = hasResponded,
    canRespond = canRespond,
)

fun SurveyDetailsDto.toDomain(): SurveyDetails {
    val submitted = responses.mapNotNull { it.submittedAt.toActivityInstantOrNull() }
    return SurveyDetails(
        id = id,
        title = title.cleaned(),
        description = description.cleaned(),
        canRespond = canRespond && isActive,
        allowMultipleResponses = allowMultipleResponses,
        questions = questions.sortedBy { it.sortOrder }.map { it.toDomain() },
        lastSubmittedAt = submitted.maxOrNull(),
        hasResponded = responses.isNotEmpty(),
    )
}

fun SurveyQuestionDto.toDomain(): SurveyQuestion = SurveyQuestion(
    id = id,
    text = questionText.cleaned(),
    type = questionType.toQuestionType(),
    options = options.orEmpty().mapNotNull { it.cleaned() },
    isRequired = isRequired,
)

private fun String?.toQuestionType(): SurveyQuestionType = when (normalized()) {
    "text" -> SurveyQuestionType.Text
    "single_choice" -> SurveyQuestionType.SingleChoice
    "multiple_choice" -> SurveyQuestionType.MultipleChoice
    "number" -> SurveyQuestionType.Number
    "boolean" -> SurveyQuestionType.Boolean
    "rating" -> SurveyQuestionType.Rating
    else -> SurveyQuestionType.Unknown
}

/**
 * Значение ответа в формате сервера: строка для text и single_choice,
 * число для number и rating, логическое значение для boolean,
 * массив строк для multiple_choice.
 */
fun SurveyAnswer.toJsonValue(): JsonElement = when (this) {
    is SurveyAnswer.Text -> JsonPrimitive(value)
    is SurveyAnswer.SingleChoice -> JsonPrimitive(option)
    is SurveyAnswer.MultipleChoice -> JsonArray(options.map { JsonPrimitive(it) })
    // Целое число отправляем без «.0»: так его понимает и целочисленная проверка.
    is SurveyAnswer.Number -> if (value % 1.0 == 0.0 && value in LONG_RANGE) {
        JsonPrimitive(value.toLong())
    } else {
        JsonPrimitive(value)
    }
    is SurveyAnswer.Bool -> JsonPrimitive(value)
    is SurveyAnswer.Rating -> JsonPrimitive(value)
}

fun Map<Long, SurveyAnswer>.toAnswerDtos(): List<SurveyAnswerDto> =
    entries
        .sortedBy { it.key }
        .map { (questionId, answer) -> SurveyAnswerDto(questionId, answer.toJsonValue()) }

private val LONG_RANGE = Long.MIN_VALUE.toDouble()..Long.MAX_VALUE.toDouble()

// Квесты

fun ActivityQuestListItemDto.toDomain(): Quest = Quest(
    id = id,
    title = title.cleaned(),
    description = description.cleaned(),
    rewardAmount = rewardAmount.coerceAtLeast(0),
    stepsCount = stepsCount.coerceAtLeast(0),
    participationStatus = participationStatus.cleaned()?.toParticipationStatus(),
)

fun QuestDetailsDto.toDomain(): QuestDetails = QuestDetails(
    id = id,
    title = title.cleaned(),
    description = description.cleaned(),
    rewardAmount = rewardAmount.coerceAtLeast(0),
    isActive = isActive,
    endAt = endAt.toActivityInstantOrNull(),
    participation = participation?.toDomain(),
    steps = steps.sortedBy { it.sortOrder }.map { it.toDomain() },
)

fun QuestParticipationDto.toDomain(): QuestParticipation = QuestParticipation(
    status = status.toParticipationStatus(),
    rewardClaimed = rewardClaimed,
)

fun QuestStepDto.toDomain(): QuestStep = QuestStep(
    id = id,
    title = title.cleaned(),
    description = description.cleaned(),
    completionType = when (completionType.normalized()) {
        "manual" -> QuestStepType.Manual
        "proof" -> QuestStepType.Proof
        "automatic" -> QuestStepType.Automatic
        else -> QuestStepType.Unknown
    },
    isRequired = isRequired,
    isCompleted = isCompleted,
    proofStatus = proofStatus.cleaned()?.let {
        when (it.normalized()) {
            "pending" -> ProofStatus.Pending
            "approved" -> ProofStatus.Approved
            "rejected" -> ProofStatus.Rejected
            else -> ProofStatus.Unknown
        }
    },
    proofStoragePath = proofStoragePath.cleaned(),
    proofComment = proofComment.cleaned(),
)

private fun String?.toParticipationStatus(): QuestParticipationStatus = when (normalized()) {
    "in_progress" -> QuestParticipationStatus.InProgress
    "completed" -> QuestParticipationStatus.Completed
    "cancelled", "canceled" -> QuestParticipationStatus.Cancelled
    else -> QuestParticipationStatus.Unknown
}

// Мероприятия

fun EventListItemDto.toDomain(): Event = Event(
    id = id,
    title = title.cleaned(),
    description = description.cleaned(),
    startAt = startAt.toActivityInstantOrNull(),
    location = location.cleaned(),
    rewardAmount = rewardAmount.coerceAtLeast(0),
    registrationStatus = registrationStatus.cleaned()?.toRegistrationStatus(),
)

fun EventDetailsDto.toDomain(): EventDetails = EventDetails(
    id = id,
    title = title.cleaned(),
    description = description.cleaned(),
    startAt = startAt.toActivityInstantOrNull(),
    endAt = endAt.toActivityInstantOrNull(),
    location = location.cleaned(),
    maxParticipants = maxParticipants?.takeIf { it > 0 },
    registeredCount = registeredCount.coerceAtLeast(0),
    rewardAmount = rewardAmount.coerceAtLeast(0),
    isOpen = isOpen && isActive,
    registration = registration?.toDomain(),
)

fun EventRegistrationDto.toDomain(): EventRegistration = EventRegistration(
    status = status.toRegistrationStatus(),
    rewardClaimed = rewardClaimed,
)

private fun String?.toRegistrationStatus(): EventRegistrationStatus = when (normalized()) {
    "registered" -> EventRegistrationStatus.Registered
    "attended" -> EventRegistrationStatus.Attended
    "cancelled", "canceled" -> EventRegistrationStatus.Cancelled
    "no_show" -> EventRegistrationStatus.NoShow
    else -> EventRegistrationStatus.Unknown
}

// Общее

/** Даты сервера в UTC вида «2026-10-23T19:38:22.939Z»; на случай без зоны считаем UTC. */
fun String?.toActivityInstantOrNull(): Instant? {
    val value = cleaned() ?: return null
    return parseOrNull { Instant.parse(value) }
        ?: parseOrNull { OffsetDateTime.parse(value).toInstant() }
        ?: parseOrNull { LocalDateTime.parse(value).toInstant(ZoneOffset.UTC) }
}

private inline fun parseOrNull(parse: () -> Instant): Instant? =
    try {
        parse()
    } catch (e: DateTimeParseException) {
        null
    }

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private fun String?.normalized(): String? = cleaned()?.lowercase()
