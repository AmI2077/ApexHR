package com.example.apexhr.features.thanks.ui.models

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.profile.ui.models.UserLabel
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.Thank
import com.example.apexhr.features.thanks.domain.ThanksOverview
import com.example.apexhr.features.thanks.domain.ThanksSendError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class ThanksUiMapperTest {

    private val anna = Colleague(id = "anna", name = "Анна Смирнова", position = "Аналитик", department = "Маркетинг")
    private val ivan = Colleague(id = "ivan", name = "Иван Петров", position = "Разработчик", department = "ИТ")
    private val noName = Colleague(id = "x", name = null, position = "Employee", department = null)
    private val colleagues = listOf(anna, ivan, noName)

    private val overview = ThanksOverview(
        colleagues = colleagues,
        received = listOf(
            Thank(
                id = 5,
                counterpartId = "ivan",
                counterpartName = "Иван Петров",
                amount = 20,
                message = "Спасибо",
                createdAt = Instant.parse("2026-10-25T22:30:00Z"),
            ),
        ),
        sent = listOf(
            Thank(id = 6, counterpartId = "gone", counterpartName = null, amount = 10, message = null, createdAt = null),
        ),
    )

    private fun state(form: ThanksForm = ThanksForm()) = overview.toUiState(form, zone = ZoneOffset.ofHours(3))

    @Test
    fun `empty query keeps all colleagues`() {
        assertEquals(colleagues, colleagues.search("  "))
    }

    @Test
    fun `search ignores case and looks at name position and department`() {
        assertEquals(listOf(anna), colleagues.search("анна"))
        assertEquals(listOf(ivan), colleagues.search("РАЗРАБ"))
        assertEquals(listOf(anna), colleagues.search("маркетинг"))
        assertEquals(listOf(noName), colleagues.search("employee"))
    }

    @Test
    fun `every word of query must match`() {
        assertEquals(listOf(ivan), colleagues.search("иван ит"))
        assertEquals(emptyList<Colleague>(), colleagues.search("иван маркетинг"))
    }

    @Test
    fun `default form selects first amount and nothing else`() {
        val state = state()

        assertEquals(listOf(10L, 20L, 30L, 50L), state.amountOptions)
        assertEquals(10L, state.selectedAmount)
        assertNull(state.selectedColleagueId)
        assertFalse(state.canSend)
        assertEquals(THANKS_MESSAGE_MAX_LENGTH, state.messageMaxLength)
    }

    @Test
    fun `selection survives search that hides selected colleague`() {
        val state = state(ThanksForm(searchQuery = "иван", selectedColleagueId = "anna"))

        assertEquals(listOf("ivan"), state.colleagues.map { it.id })
        assertEquals("anna", state.selectedColleagueId)
        assertEquals("Анна Смирнова", state.selectedColleagueName)
        assertTrue(state.canSend)
    }

    @Test
    fun `selected colleague missing from list is dropped`() {
        val state = state(ThanksForm(selectedColleagueId = "fired"))

        assertNull(state.selectedColleagueId)
        assertFalse(state.canSend)
    }

    @Test
    fun `cannot send while sending`() {
        assertFalse(state(ThanksForm(selectedColleagueId = "anna", isSending = true)).canSend)
    }

    @Test
    fun `nothing found only when query is not blank`() {
        assertTrue(state(ThanksForm(searchQuery = "zzz")).isNothingFound)
        assertFalse(state().isNothingFound)
    }

    @Test
    fun `colleague row shows position and department`() {
        assertEquals(
            ColleagueUiModel(
                id = "anna",
                name = "Анна Смирнова",
                position = listOf(UserLabel.Text("Аналитик"), UserLabel.Text("Маркетинг")),
            ),
            anna.toUiModel(),
        )
        assertEquals(
            ColleagueUiModel(
                id = "x",
                name = null,
                position = listOf(UserLabel.Resource(R.string.profile_position_employee)),
            ),
            noName.toUiModel(),
        )
    }

    @Test
    fun `thanks are mapped with local date and unknown name`() {
        val state = state()

        assertEquals(
            listOf(ThankUiModel(id = "5", name = "Иван Петров", amount = 20, message = "Спасибо", date = "26.10.2026")),
            state.received,
        )
        assertEquals(
            listOf(ThankUiModel(id = "6", name = null, amount = 10, message = null, date = null)),
            state.sent,
        )
    }

    @Test
    fun `message is limited to max length`() {
        val long = "а".repeat(THANKS_MESSAGE_MAX_LENGTH + 10)

        assertEquals(THANKS_MESSAGE_MAX_LENGTH, long.limitThanksMessage().length)
        assertEquals("Спасибо", "Спасибо".limitThanksMessage())
    }

    @Test
    fun `weekly limit message is shown as is`() {
        assertEquals(
            ThanksNotice(R.string.thanks_error_server_message, listOf("Weekly limit exceeded"), isError = true),
            ThanksSendError.Rejected("Weekly limit exceeded").toNotice(),
        )
    }

    @Test
    fun `rejection without message has fallback text`() {
        assertEquals(
            ThanksNotice(R.string.thanks_error_send, isError = true),
            ThanksSendError.Rejected(null).toNotice(),
        )
        assertEquals(
            ThanksNotice(R.string.thanks_error_send, isError = true),
            ThanksSendError.Data(DataError.Unknown).toNotice(),
        )
    }

    @Test
    fun `not found and forbidden have own texts`() {
        assertEquals(R.string.thanks_error_not_found, ThanksSendError.NotFound.toNotice().textRes)
        assertEquals(R.string.thanks_error_forbidden, ThanksSendError.Forbidden.toNotice().textRes)
        assertFalse(THANKS_SENT_NOTICE.isError)
    }
}
