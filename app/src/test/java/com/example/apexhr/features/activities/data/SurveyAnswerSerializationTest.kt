package com.example.apexhr.features.activities.data

import com.example.apexhr.features.activities.data.dto.SubmitSurveyRequest
import com.example.apexhr.features.activities.domain.SurveyAnswer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SurveyAnswerSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    private fun encode(answer: SurveyAnswer): String = answer.toJsonValue().toString()

    @Test
    fun `answer values have server types`() {
        assertEquals("\"Офис\"", encode(SurveyAnswer.SingleChoice("Офис")))
        assertEquals("\"Больше кофе\"", encode(SurveyAnswer.Text("Больше кофе")))
        assertEquals("[\"A\",\"B\"]", encode(SurveyAnswer.MultipleChoice(listOf("A", "B"))))
        assertEquals("[]", encode(SurveyAnswer.MultipleChoice(emptyList())))
        assertEquals("true", encode(SurveyAnswer.Bool(true)))
        assertEquals("4", encode(SurveyAnswer.Rating(4)))
    }

    @Test
    fun `whole numbers are sent without fraction`() {
        assertEquals("42", encode(SurveyAnswer.Number(42.0)))
        assertEquals("-3", encode(SurveyAnswer.Number(-3.0)))
        assertEquals("2.5", encode(SurveyAnswer.Number(2.5)))
    }

    @Test
    fun `request body matches swagger`() {
        val answers = mapOf(
            11L to SurveyAnswer.SingleChoice("Офис"),
            10L to SurveyAnswer.Rating(5),
            12L to SurveyAnswer.MultipleChoice(listOf("Кофе")),
        )

        val body = json.encodeToString(SubmitSurveyRequest.serializer(), SubmitSurveyRequest(answers.toAnswerDtos()))

        assertEquals(
            """{"answers":[{"questionId":10,"value":5},{"questionId":11,"value":"Офис"},""" +
                """{"questionId":12,"value":["Кофе"]}]}""",
            body,
        )
    }
}
