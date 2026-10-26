package com.example.apexhr.features.activities.ui.survey.models

import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.SurveyAnswer
import com.example.apexhr.features.activities.domain.SurveyDetails
import com.example.apexhr.features.activities.domain.SurveyQuestion
import com.example.apexhr.features.activities.domain.SurveyQuestionType
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.models.formatActivityDate
import java.time.ZoneId

/** Диапазон оценки: в Swagger не описан, считаем 1–5. */
val SURVEY_RATING_RANGE = 1..5

/** @param zone часовой пояс, в котором показываем даты (по умолчанию — устройства). */
fun SurveyDetails.toUiModel(zone: ZoneId = ZoneId.systemDefault()): SurveyDetailsUiModel {
    val submittedAt = lastSubmittedAt?.formatActivityDate(zone)
    val hasUnsupportedRequired = questions.any { it.isRequired && it.type == SurveyQuestionType.Unknown }
    val canAnswer = canRespond && !hasUnsupportedRequired
    val note = when {
        canRespond && hasUnsupportedRequired -> ActivityMessage(R.string.survey_note_unsupported)
        canRespond && hasResponded -> if (submittedAt != null) {
            ActivityMessage(R.string.survey_note_answered_again, listOf(submittedAt))
        } else {
            ActivityMessage(R.string.survey_note_answered_again_no_date)
        }

        canRespond -> null
        hasResponded -> if (submittedAt != null) {
            ActivityMessage(R.string.survey_note_answered, listOf(submittedAt))
        } else {
            ActivityMessage(R.string.activities_status_survey_answered)
        }

        else -> ActivityMessage(R.string.survey_note_closed)
    }
    return SurveyDetailsUiModel(
        title = title,
        questions = if (canAnswer) questions.map { it.toUiModel() } else emptyList(),
        canAnswer = canAnswer,
        note = note,
    )
}

private fun SurveyQuestion.toUiModel() = SurveyQuestionUiModel(
    id = id,
    text = text,
    type = type,
    options = options,
    isRequired = isRequired,
)

/** Черновик после нажатия на вариант: одиночный выбор заменяет, множественный переключает. */
fun SurveyAnswerInput.withOption(type: SurveyQuestionType, option: String): SurveyAnswerInput = when (type) {
    SurveyQuestionType.SingleChoice -> copy(selected = setOf(option))
    SurveyQuestionType.MultipleChoice ->
        copy(selected = if (option in selected) selected - option else selected + option)

    else -> this
}

/** Итог проверки формы: ответы к отправке или ошибки по вопросам. */
data class SurveyValidation(
    val answers: Map<Long, SurveyAnswer>,
    val errors: Map<Long, SurveyFieldError>,
)

/**
 * Пустые необязательные вопросы не отправляются; вопросы неизвестного типа пропускаются.
 * Число принимается и с запятой.
 */
fun validateSurvey(
    questions: List<SurveyQuestion>,
    inputs: Map<Long, SurveyAnswerInput>,
): SurveyValidation {
    val answers = mutableMapOf<Long, SurveyAnswer>()
    val errors = mutableMapOf<Long, SurveyFieldError>()
    questions.forEach { question ->
        val input = inputs[question.id] ?: SurveyAnswerInput()
        when (val result = input.toAnswer(question)) {
            is AnswerResult.Value -> answers[question.id] = result.answer
            AnswerResult.Empty -> if (question.isRequired && question.type != SurveyQuestionType.Unknown) {
                errors[question.id] = SurveyFieldError.Required
            }

            AnswerResult.InvalidNumber -> errors[question.id] = SurveyFieldError.InvalidNumber
        }
    }
    return SurveyValidation(answers, errors)
}

private sealed interface AnswerResult {
    data class Value(val answer: SurveyAnswer) : AnswerResult
    data object Empty : AnswerResult
    data object InvalidNumber : AnswerResult
}

private fun SurveyAnswerInput.toAnswer(question: SurveyQuestion): AnswerResult {
    val answer: SurveyAnswer? = when (question.type) {
        SurveyQuestionType.Text -> text.trim().takeIf { it.isNotEmpty() }?.let { SurveyAnswer.Text(it) }
        SurveyQuestionType.Number -> {
            val trimmed = text.trim()
            if (trimmed.isEmpty()) return AnswerResult.Empty
            val number = trimmed.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
                ?: return AnswerResult.InvalidNumber
            SurveyAnswer.Number(number)
        }

        SurveyQuestionType.SingleChoice -> selected.firstOrNull()?.let { SurveyAnswer.SingleChoice(it) }
        // Порядок вариантов — как в вопросе, а не как нажимали.
        SurveyQuestionType.MultipleChoice -> question.options.filter { it in selected }
            .takeIf { it.isNotEmpty() }
            ?.let { SurveyAnswer.MultipleChoice(it) }

        SurveyQuestionType.Boolean -> bool?.let { SurveyAnswer.Bool(it) }
        SurveyQuestionType.Rating -> rating?.let { SurveyAnswer.Rating(it) }
        SurveyQuestionType.Unknown -> null
    }
    return answer?.let { AnswerResult.Value(it) } ?: AnswerResult.Empty
}
