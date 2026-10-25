package com.example.apexhr.features.activities.ui.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.domain.SurveyDetails
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.common.toActivityMessage
import com.example.apexhr.features.activities.ui.survey.models.SURVEY_RATING_RANGE
import com.example.apexhr.features.activities.ui.survey.models.SurveyAnswerInput
import com.example.apexhr.features.activities.ui.survey.models.SurveyDetailsScreenCallbacks
import com.example.apexhr.features.activities.ui.survey.models.SurveyDetailsScreenState
import com.example.apexhr.features.activities.ui.survey.models.toUiModel
import com.example.apexhr.features.activities.ui.survey.models.validateSurvey
import com.example.apexhr.features.activities.ui.survey.models.withOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Прохождение опроса: черновики ответов, проверка обязательных вопросов и отправка.
 * После успешной отправки детали перечитываются (форма сбрасывается). «Назад» задаёт навигация.
 */
class SurveyDetailsViewModel(
    private val surveyId: Long,
    private val repository: ActivitiesRepository,
) : ViewModel(), SurveyDetailsScreenCallbacks {

    private val _state = MutableStateFlow<SurveyDetailsScreenState>(SurveyDetailsScreenState.Loading)
    val state: StateFlow<SurveyDetailsScreenState> = _state.asStateFlow()

    private val _messages = Channel<ActivityMessage>(Channel.BUFFERED)
    val messages: Flow<ActivityMessage> = _messages.receiveAsFlow()

    private var details: SurveyDetails? = null
    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()

    override fun onTextChange(questionId: Long, text: String) = updateInput(questionId) { copy(text = text) }

    override fun onOptionClick(questionId: Long, option: String) {
        val type = details?.questions?.firstOrNull { it.id == questionId }?.type ?: return
        updateInput(questionId) { withOption(type, option) }
    }

    override fun onBooleanSelect(questionId: Long, value: Boolean) = updateInput(questionId) { copy(bool = value) }

    override fun onRatingSelect(questionId: Long, value: Int) {
        if (value !in SURVEY_RATING_RANGE) return
        updateInput(questionId) { copy(rating = value) }
    }

    override fun onSubmitClick() {
        val content = _state.value as? SurveyDetailsScreenState.Content ?: return
        val survey = details ?: return
        if (content.isSubmitting || !content.survey.canAnswer) return
        val validation = validateSurvey(survey.questions, content.inputs)
        if (validation.errors.isNotEmpty()) {
            _state.value = content.copy(errors = validation.errors)
            _messages.trySend(ActivityMessage(R.string.survey_message_fix_errors))
            return
        }
        _state.value = content.copy(errors = emptyMap(), isSubmitting = true)
        viewModelScope.launch {
            when (val result = repository.submitSurvey(surveyId, validation.answers)) {
                ActivityActionResult.Success -> {
                    _messages.send(ActivityMessage(R.string.survey_message_submitted))
                    reload(resetInputs = true)
                }

                is ActivityActionResult.Failure -> {
                    _messages.send(result.error.toActivityMessage())
                    // Ответы не теряем: пользователь может исправить и отправить снова.
                    updateContent { copy(isSubmitting = false) }
                }
            }
        }
    }

    private fun updateInput(questionId: Long, transform: SurveyAnswerInput.() -> SurveyAnswerInput) =
        updateContent {
            if (isSubmitting) return@updateContent this
            val input = (inputs[questionId] ?: SurveyAnswerInput()).transform()
            copy(inputs = inputs + (questionId to input), errors = errors - questionId)
        }

    private fun updateContent(transform: SurveyDetailsScreenState.Content.() -> SurveyDetailsScreenState.Content) {
        val content = _state.value as? SurveyDetailsScreenState.Content ?: return
        _state.value = content.transform()
    }

    /** Перечитывает детали; при ошибке уже показанные данные остаются. */
    private suspend fun reload(resetInputs: Boolean) {
        val result = repository.getSurveyDetails(surveyId)
        val current = _state.value as? SurveyDetailsScreenState.Content
        _state.value = when (result) {
            is DataResult.Success -> {
                details = result.data
                SurveyDetailsScreenState.Content(
                    survey = result.data.toUiModel(),
                    inputs = if (resetInputs || current == null) emptyMap() else current.inputs,
                    errors = emptyMap(),
                    isSubmitting = false,
                )
            }

            is DataResult.Failure -> current?.copy(
                inputs = if (resetInputs) emptyMap() else current.inputs,
                isSubmitting = false,
            ) ?: SurveyDetailsScreenState.Error(result.error)
        }
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = SurveyDetailsScreenState.Loading
        loadJob = viewModelScope.launch { reload(resetInputs = true) }
    }
}
