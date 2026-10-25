package com.example.apexhr.features.activities.ui.survey

import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.SurveyAnswer
import com.example.apexhr.features.activities.domain.SurveyDetails
import com.example.apexhr.features.activities.domain.SurveyQuestion
import com.example.apexhr.features.activities.domain.SurveyQuestionType
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.survey.models.SurveyAnswerInput
import com.example.apexhr.features.activities.ui.survey.models.SurveyFieldError
import com.example.apexhr.features.activities.ui.survey.models.toUiModel
import com.example.apexhr.features.activities.ui.survey.models.validateSurvey
import com.example.apexhr.features.activities.ui.survey.models.withOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class SurveyDetailsUiMapperTest {

    private val moscow = ZoneId.of("Europe/Moscow")

    private fun question(
        id: Long,
        type: SurveyQuestionType,
        isRequired: Boolean = true,
        options: List<String> = emptyList(),
    ) = SurveyQuestion(id = id, text = "Вопрос $id", type = type, options = options, isRequired = isRequired)

    private fun survey(
        canRespond: Boolean = true,
        hasResponded: Boolean = false,
        lastSubmittedAt: Instant? = null,
        questions: List<SurveyQuestion> = listOf(question(1, SurveyQuestionType.Text)),
    ) = SurveyDetails(
        id = 1,
        title = "Пульс команды",
        description = null,
        canRespond = canRespond,
        allowMultipleResponses = canRespond && hasResponded,
        questions = questions,
        lastSubmittedAt = lastSubmittedAt,
        hasResponded = hasResponded,
    )

    @Test
    fun `open survey shows form without note`() {
        val model = survey().toUiModel(moscow)

        assertTrue(model.canAnswer)
        assertNull(model.note)
        assertEquals(listOf(1L), model.questions.map { it.id })
    }

    @Test
    fun `answered survey shows date and hides form unless repeat allowed`() {
        val submittedAt = Instant.parse("2026-10-22T22:46:00Z")

        val answered = survey(canRespond = false, hasResponded = true, lastSubmittedAt = submittedAt).toUiModel(moscow)
        assertFalse(answered.canAnswer)
        assertTrue(answered.questions.isEmpty())
        assertEquals(ActivityMessage(R.string.survey_note_answered, listOf("23.10.2026 01:46")), answered.note)

        val again = survey(hasResponded = true, lastSubmittedAt = submittedAt).toUiModel(moscow)
        assertTrue(again.canAnswer)
        assertEquals(ActivityMessage(R.string.survey_note_answered_again, listOf("23.10.2026 01:46")), again.note)
    }

    @Test
    fun `closed survey and unsupported required question block the form`() {
        val closed = survey(canRespond = false).toUiModel(moscow)
        assertFalse(closed.canAnswer)
        assertEquals(ActivityMessage(R.string.survey_note_closed), closed.note)

        val unsupported = survey(questions = listOf(question(1, SurveyQuestionType.Unknown))).toUiModel(moscow)
        assertFalse(unsupported.canAnswer)
        assertEquals(ActivityMessage(R.string.survey_note_unsupported), unsupported.note)

        val optionalUnknown = survey(
            questions = listOf(question(1, SurveyQuestionType.Unknown, isRequired = false)),
        ).toUiModel(moscow)
        assertTrue(optionalUnknown.canAnswer)
    }

    @Test
    fun `single choice replaces and multiple choice toggles options`() {
        val single = SurveyAnswerInput()
            .withOption(SurveyQuestionType.SingleChoice, "А")
            .withOption(SurveyQuestionType.SingleChoice, "Б")
        assertEquals(setOf("Б"), single.selected)

        val multiple = SurveyAnswerInput()
            .withOption(SurveyQuestionType.MultipleChoice, "А")
            .withOption(SurveyQuestionType.MultipleChoice, "Б")
            .withOption(SurveyQuestionType.MultipleChoice, "А")
        assertEquals(setOf("Б"), multiple.selected)
    }

    @Test
    fun `valid inputs become answers of matching types`() {
        val questions = listOf(
            question(1, SurveyQuestionType.Text),
            question(2, SurveyQuestionType.SingleChoice, options = listOf("А", "Б")),
            question(3, SurveyQuestionType.MultipleChoice, options = listOf("А", "Б", "В")),
            question(4, SurveyQuestionType.Number),
            question(5, SurveyQuestionType.Boolean),
            question(6, SurveyQuestionType.Rating),
        )
        val inputs = mapOf(
            1L to SurveyAnswerInput(text = "  Всё хорошо "),
            2L to SurveyAnswerInput(selected = setOf("Б")),
            3L to SurveyAnswerInput(selected = setOf("В", "А")),
            4L to SurveyAnswerInput(text = "2,5"),
            5L to SurveyAnswerInput(bool = false),
            6L to SurveyAnswerInput(rating = 4),
        )

        val result = validateSurvey(questions, inputs)

        assertTrue(result.errors.isEmpty())
        assertEquals(
            mapOf(
                1L to SurveyAnswer.Text("Всё хорошо"),
                2L to SurveyAnswer.SingleChoice("Б"),
                3L to SurveyAnswer.MultipleChoice(listOf("А", "В")),
                4L to SurveyAnswer.Number(2.5),
                5L to SurveyAnswer.Bool(false),
                6L to SurveyAnswer.Rating(4),
            ),
            result.answers,
        )
    }

    @Test
    fun `missing required answers and bad numbers are errors, empty optional are skipped`() {
        val questions = listOf(
            question(1, SurveyQuestionType.Text),
            question(2, SurveyQuestionType.Rating),
            question(3, SurveyQuestionType.Number, isRequired = false),
            question(4, SurveyQuestionType.Text, isRequired = false),
            question(5, SurveyQuestionType.Unknown, isRequired = false),
        )
        val inputs = mapOf(
            1L to SurveyAnswerInput(text = "   "),
            3L to SurveyAnswerInput(text = "много"),
        )

        val result = validateSurvey(questions, inputs)

        assertEquals(
            mapOf(
                1L to SurveyFieldError.Required,
                2L to SurveyFieldError.Required,
                3L to SurveyFieldError.InvalidNumber,
            ),
            result.errors,
        )
        assertTrue(result.answers.isEmpty())
    }
}
