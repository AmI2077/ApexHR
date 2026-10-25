package com.example.apexhr.features.activities.ui.survey.models

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.activities.domain.SurveyQuestionType
import com.example.apexhr.features.activities.ui.common.ActivityMessage

data class SurveyDetailsUiModel(
    val title: String?,
    val description: String?,
    val questions: List<SurveyQuestionUiModel>,
    /** Показывать форму и кнопку «Отправить». */
    val canAnswer: Boolean,
    /** Пояснение над формой или вместо неё: уже отвечали, опрос закрыт и т. п. */
    val note: ActivityMessage?,
) {
    companion object {
        val Preview = SurveyDetailsUiModel(
            title = "Пульс команды",
            description = "Пара вопросов о работе в команде",
            questions = listOf(
                SurveyQuestionUiModel(1, "Как настроение?", SurveyQuestionType.Rating, emptyList(), isRequired = true),
                SurveyQuestionUiModel(
                    2,
                    "Что улучшить?",
                    SurveyQuestionType.MultipleChoice,
                    listOf("Офис", "Процессы", "Обучение"),
                    isRequired = false,
                ),
                SurveyQuestionUiModel(3, "Комментарий", SurveyQuestionType.Text, emptyList(), isRequired = false),
            ),
            canAnswer = true,
            note = ActivityMessage(R.string.survey_note_answered_again, listOf("23.10.2026 01:46")),
        )
    }
}

data class SurveyQuestionUiModel(
    val id: Long,
    val text: String?,
    val type: SurveyQuestionType,
    val options: List<String>,
    val isRequired: Boolean,
)

/**
 * Черновик ответа на вопрос. Какое поле значимо, зависит от типа вопроса:
 * [text] — текст и число, [selected] — выбор вариантов, [bool] — да/нет, [rating] — оценка.
 */
data class SurveyAnswerInput(
    val text: String = "",
    val selected: Set<String> = emptySet(),
    val bool: Boolean? = null,
    val rating: Int? = null,
)

enum class SurveyFieldError { Required, InvalidNumber }

sealed interface SurveyDetailsScreenState {
    data object Loading : SurveyDetailsScreenState
    data class Error(val error: DataError) : SurveyDetailsScreenState

    /**
     * @param inputs черновики ответов по id вопроса.
     * @param errors ошибки проверки по id вопроса, показываются после попытки отправить.
     */
    data class Content(
        val survey: SurveyDetailsUiModel,
        val inputs: Map<Long, SurveyAnswerInput>,
        val errors: Map<Long, SurveyFieldError>,
        val isSubmitting: Boolean,
    ) : SurveyDetailsScreenState
}

interface SurveyDetailsScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()
    fun onTextChange(questionId: Long, text: String)
    fun onOptionClick(questionId: Long, option: String)
    fun onBooleanSelect(questionId: Long, value: Boolean)
    fun onRatingSelect(questionId: Long, value: Int)
    fun onSubmitClick()

    companion object {
        val Empty = object : SurveyDetailsScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onTextChange(questionId: Long, text: String) = Unit
            override fun onOptionClick(questionId: Long, option: String) = Unit
            override fun onBooleanSelect(questionId: Long, value: Boolean) = Unit
            override fun onRatingSelect(questionId: Long, value: Int) = Unit
            override fun onSubmitClick() = Unit
        }
    }
}
