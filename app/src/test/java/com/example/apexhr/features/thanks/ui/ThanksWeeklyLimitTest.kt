package com.example.apexhr.features.thanks.ui

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.ManagerBonusAccess
import com.example.apexhr.features.thanks.domain.PEER_THANKS_AMOUNT
import com.example.apexhr.features.thanks.domain.Thank
import com.example.apexhr.features.thanks.domain.ThanksHistory
import com.example.apexhr.features.thanks.domain.ThanksRepository
import com.example.apexhr.features.thanks.domain.ThanksSendResult
import com.example.apexhr.features.thanks.domain.recipientsThankedRecently
import com.example.apexhr.features.thanks.ui.models.ThanksScreenState
import com.example.apexhr.features.thanks.ui.models.ThanksUiState
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThanksWeeklyLimitTest {

    private val now = Instant.parse("2026-11-18T12:00:00Z")
    private val anna = Colleague(id = "anna", name = "Анна", position = null, department = null)
    private val ivan = Colleague(id = "ivan", name = "Иван", position = null, department = null)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sent(to: String, ago: Duration) = Thank(
        id = 1, counterpartId = to, counterpartName = null, amount = PEER_THANKS_AMOUNT,
        message = null, createdAt = now.minus(ago),
    )

    private fun ThanksViewModel.ui(): ThanksUiState = (state.value as ThanksScreenState.Content).uiState

    @Test
    fun `peer thanks amount is 25 apexes like on the web`() {
        assertEquals(25L, PEER_THANKS_AMOUNT)
    }

    @Test
    fun `only thanks of the last 7 days count`() {
        val thanks = listOf(
            sent("anna", Duration.ofDays(6)),
            sent("ivan", Duration.ofDays(7).plusMinutes(1)),
            sent("olga", Duration.ofDays(7)),
            Thank(id = 2, counterpartId = "petr", counterpartName = null, amount = 25, message = null, createdAt = null),
        )

        assertEquals(setOf("anna", "olga"), thanks.recipientsThankedRecently(now))
    }

    @Test
    fun `colleague thanked this week cannot be thanked again`() {
        val repository = LimitFakeRepository(sent = listOf(sent("anna", Duration.ofDays(2))))
        val viewModel = ThanksViewModel(repository) { now }

        viewModel.onColleagueClick("anna")
        viewModel.onMessageChange("Спасибо!")

        assertTrue(viewModel.ui().isSelectedThankedRecently)
        assertFalse(viewModel.ui().canSend)
        viewModel.onSendClick()
        assertEquals(0, repository.sendCalls)

        viewModel.onColleagueClick("ivan")
        assertFalse(viewModel.ui().isSelectedThankedRecently)
        assertTrue(viewModel.ui().canSend)
        assertEquals(25L, viewModel.ui().amount)
    }

    @Test
    fun `after sending the same colleague is locked for the week`() {
        val repository = LimitFakeRepository(sent = emptyList())
        val viewModel = ThanksViewModel(repository) { now }

        viewModel.onColleagueClick("ivan")
        viewModel.onMessageChange("Спасибо!")
        viewModel.onSendClick()
        assertEquals(1, repository.sendCalls)

        viewModel.onColleagueClick("ivan")
        viewModel.onMessageChange("Ещё раз спасибо")
        assertTrue(viewModel.ui().isSelectedThankedRecently)
        assertFalse(viewModel.ui().canSend)
    }

    @Test
    fun `history error does not block the form`() {
        val repository = LimitFakeRepository(sent = emptyList(), historyError = true)
        val viewModel = ThanksViewModel(repository) { now }

        viewModel.onColleagueClick("anna")
        viewModel.onMessageChange("Спасибо!")

        assertTrue(viewModel.ui().canSend)
    }

    private inner class LimitFakeRepository(
        private val sent: List<Thank>,
        private val historyError: Boolean = false,
    ) : ThanksRepository {
        var sendCalls = 0

        override suspend fun loadColleagues(): DataResult<List<Colleague>> = DataResult.Success(listOf(anna, ivan))
        override suspend fun loadHistory(): DataResult<ThanksHistory> =
            if (historyError) DataResult.Failure(DataError.Network)
            else DataResult.Success(ThanksHistory(received = emptyList(), sent = sent))

        override suspend fun sendThanks(toUserId: String, message: String): ThanksSendResult {
            sendCalls++
            return ThanksSendResult.Success
        }

        override suspend fun loadManagerBonusAccess(): DataResult<ManagerBonusAccess> =
            DataResult.Success(ManagerBonusAccess.None)

        override suspend fun sendManagerBonus(toUserId: String, amount: Long, message: String): ThanksSendResult =
            error("not used")

        override val thanksSent: Flow<Unit> = emptyFlow()
    }
}