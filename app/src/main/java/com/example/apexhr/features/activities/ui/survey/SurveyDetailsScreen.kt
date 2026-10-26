package com.example.apexhr.features.activities.ui.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.transparent
import com.example.apexhr.features.activities.domain.SurveyQuestionType
import com.example.apexhr.features.activities.ui.common.ActivityActionButton
import com.example.apexhr.features.activities.ui.common.ActivityDetailsContent
import com.example.apexhr.features.activities.ui.common.ActivityDetailsLayout
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.common.ActivityMessagesEffect
import com.example.apexhr.features.activities.ui.common.ActivityNoteText
import com.example.apexhr.features.activities.ui.survey.models.SURVEY_RATING_RANGE
import com.example.apexhr.features.activities.ui.survey.models.SurveyAnswerInput
import com.example.apexhr.features.activities.ui.survey.models.SurveyDetailsScreenCallbacks
import com.example.apexhr.features.activities.ui.survey.models.SurveyDetailsScreenState
import com.example.apexhr.features.activities.ui.survey.models.SurveyDetailsUiModel
import com.example.apexhr.features.activities.ui.survey.models.SurveyFieldError
import com.example.apexhr.features.activities.ui.survey.models.SurveyQuestionUiModel
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
fun SurveyDetailsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SurveyDetailsScreen(
            screenState = SurveyDetailsScreenState.Content(
                survey = SurveyDetailsUiModel.Preview,
                inputs = mapOf(2L to SurveyAnswerInput(selected = setOf("Офис"))),
                errors = mapOf(1L to SurveyFieldError.Required),
                isSubmitting = false,
            ),
            callbacks = SurveyDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SurveyDetailsScreenClosedPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SurveyDetailsScreen(
            screenState = SurveyDetailsScreenState.Content(
                survey = SurveyDetailsUiModel.Preview.copy(
                    title = "Ежеквартальный опрос о вовлечённости сотрудников и удобстве рабочих процессов",
                    questions = emptyList(),
                    canAnswer = false,
                    note = ActivityMessage(R.string.survey_note_answered, listOf("23.10.2026 01:46")),
                ),
                inputs = emptyMap(),
                errors = emptyMap(),
                isSubmitting = false,
            ),
            callbacks = SurveyDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SurveyDetailsScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SurveyDetailsScreen(
            screenState = SurveyDetailsScreenState.Error(DataError.Network),
            callbacks = SurveyDetailsScreenCallbacks.Empty,
        )
    }
}

/** Длинное название опроса в шапке переносится на вторую строку, дальше — многоточие. */
private const val SURVEY_TITLE_MAX_LINES = 2

/**
 * Опрос: вопросы всех поддерживаемых типов и отправка ответов.
 * @param messages итог отправки и ошибки, показываются в snackbar.
 */
@Composable
fun SurveyDetailsScreen(
    screenState: SurveyDetailsScreenState,
    callbacks: SurveyDetailsScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<ActivityMessage> = emptyFlow(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ActivityMessagesEffect(messages = messages, snackbarHostState = snackbarHostState)

    // Пока опрос грузится или не загрузился, в шапке общее «Опрос», потом — его название.
    val title = (screenState as? SurveyDetailsScreenState.Content)?.survey?.title
        ?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.survey_details_title)
    ActivityDetailsLayout(
        title = title,
        onBackClick = callbacks::onBackClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        titleMaxLines = SURVEY_TITLE_MAX_LINES,
    ) {
        when (screenState) {
            SurveyDetailsScreenState.Loading -> LoadingState()
            is SurveyDetailsScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is SurveyDetailsScreenState.Content -> SurveyDetailsBody(
                state = screenState,
                callbacks = callbacks,
            )
        }
    }
}

@Composable
private fun SurveyDetailsBody(
    state: SurveyDetailsScreenState.Content,
    callbacks: SurveyDetailsScreenCallbacks,
) {
    val survey = state.survey
    ActivityDetailsContent {
        survey.note?.let { note ->
            ActivityNoteText(text = stringResource(note.textRes, *note.args.toTypedArray()))
        }
        survey.questions.forEachIndexed { index, question ->
            SurveyQuestionCard(
                number = index + 1,
                question = question,
                input = state.inputs[question.id] ?: SurveyAnswerInput(),
                error = state.errors[question.id],
                enabled = !state.isSubmitting,
                callbacks = callbacks,
            )
        }
        if (survey.canAnswer) {
            ActivityActionButton(
                text = stringResource(R.string.survey_action_submit),
                inProgress = state.isSubmitting,
                onClick = callbacks::onSubmitClick,
            )
        }
    }
}

@Composable
private fun SurveyQuestionCard(
    number: Int,
    question: SurveyQuestionUiModel,
    input: SurveyAnswerInput,
    error: SurveyFieldError?,
    enabled: Boolean,
    callbacks: SurveyDetailsScreenCallbacks,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(ApexDimens.spacing12.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
    ) {
        val text = question.text ?: stringResource(R.string.activities_untitled)
        Text(
            text = if (question.isRequired) {
                stringResource(R.string.survey_question_required, number, text)
            } else {
                stringResource(R.string.survey_question, number, text)
            },
            color = primaryTextColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        when (question.type) {
            SurveyQuestionType.Text -> SurveyTextField(
                value = input.text,
                onValueChange = { callbacks.onTextChange(question.id, it) },
                enabled = enabled,
                keyboardType = KeyboardType.Text,
                singleLine = false,
            )

            SurveyQuestionType.Number -> SurveyTextField(
                value = input.text,
                onValueChange = { callbacks.onTextChange(question.id, it) },
                enabled = enabled,
                keyboardType = KeyboardType.Decimal,
                singleLine = true,
            )

            SurveyQuestionType.SingleChoice -> question.options.forEach { option ->
                SurveyRadioRow(
                    text = option,
                    selected = option in input.selected,
                    enabled = enabled,
                    onClick = { callbacks.onOptionClick(question.id, option) },
                )
            }

            SurveyQuestionType.MultipleChoice -> question.options.forEach { option ->
                SurveyCheckboxRow(
                    text = option,
                    checked = option in input.selected,
                    enabled = enabled,
                    onClick = { callbacks.onOptionClick(question.id, option) },
                )
            }

            SurveyQuestionType.Boolean -> listOf(
                true to R.string.survey_answer_yes,
                false to R.string.survey_answer_no,
            ).forEach { (value, labelRes) ->
                SurveyRadioRow(
                    text = stringResource(labelRes),
                    selected = input.bool == value,
                    enabled = enabled,
                    onClick = { callbacks.onBooleanSelect(question.id, value) },
                )
            }

            SurveyQuestionType.Rating -> SurveyRatingRow(
                selected = input.rating,
                enabled = enabled,
                onSelect = { callbacks.onRatingSelect(question.id, it) },
            )

            SurveyQuestionType.Unknown ->
                ActivityNoteText(text = stringResource(R.string.survey_question_unsupported))
        }
        error?.let {
            Text(
                text = stringResource(
                    when (it) {
                        SurveyFieldError.Required -> R.string.survey_error_required
                        SurveyFieldError.InvalidNumber -> R.string.survey_error_number
                    },
                ),
                color = negativeRedColor,
                fontSize = ApexDimens.text12.sp,
            )
        }
    }
}

@Composable
private fun SurveyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    keyboardType: KeyboardType,
    singleLine: Boolean,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = {
            Text(text = stringResource(R.string.survey_answer_placeholder), fontSize = ApexDimens.text14.sp)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ApexDimens.radius12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            focusedIndicatorColor = transparent,
            unfocusedIndicatorColor = transparent,
            disabledIndicatorColor = transparent,
            cursorColor = primaryBlueColor,
            focusedTextColor = primaryTextColor,
            unfocusedTextColor = primaryTextColor,
        ),
    )
}

@Composable
private fun SurveyRadioRow(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = primaryBlueColor),
        )
        SurveyOptionText(text = text)
    }
}

@Composable
private fun SurveyCheckboxRow(
    text: String,
    checked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = { onClick() }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = CheckboxDefaults.colors(checkedColor = primaryBlueColor),
        )
        SurveyOptionText(text = text)
    }
}

@Composable
private fun SurveyOptionText(text: String) {
    Text(
        text = text,
        color = primaryTextColor,
        fontSize = ApexDimens.text14.sp,
        modifier = Modifier.padding(start = ApexDimens.spacing8.dp),
    )
}

@Composable
private fun SurveyRatingRow(
    selected: Int?,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp)) {
        SURVEY_RATING_RANGE.forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                enabled = enabled,
                label = { Text(text = value.toString(), fontSize = ApexDimens.text14.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryBlueColor,
                    selectedLabelColor = backgroundColor,
                ),
            )
        }
    }
}
