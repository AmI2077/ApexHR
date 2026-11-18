package com.example.apexhr.features.support.ui

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.support.domain.SupportTicketStatus
import com.example.apexhr.features.support.ui.models.SupportListScreenState
import com.example.apexhr.features.support.ui.models.SupportListUiState
import com.example.apexhr.features.support.ui.models.SupportTicketScreenState
import com.example.apexhr.features.support.ui.models.SupportTicketUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupportViewModelsTest {

    private val repository = FakeSupportRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun SupportListViewModel.content(): SupportListUiState =
        (state.value as SupportListScreenState.Content).uiState

    private fun SupportTicketViewModel.content(): SupportTicketUiState =
        (state.value as SupportTicketScreenState.Content).uiState

    // Список обращений

    @Test
    fun `list loads on refresh and shows error when first load fails`() {
        val viewModel = SupportListViewModel(repository)
        assertEquals(SupportListScreenState.Loading, viewModel.state.value)

        viewModel.refresh()
        assertEquals(SupportListScreenState.Error(DataError.Network), viewModel.state.value)

        repository.pages[1] = ticketsPage(1, hasNext = false, summary(1), summary(2))
        viewModel.onRetryClick()
        assertEquals(listOf(1L, 2L), viewModel.content().items.map { it.id })
    }

    @Test
    fun `repeated refresh keeps list when it fails`() {
        repository.pages[1] = ticketsPage(1, hasNext = false, summary(1))
        val viewModel = SupportListViewModel(repository)
        viewModel.refresh()

        repository.pages.clear()
        viewModel.refresh()

        assertEquals(listOf(1L), viewModel.content().items.map { it.id })
    }

    @Test
    fun `repeated refresh shows new tickets`() {
        repository.pages[1] = ticketsPage(1, hasNext = false, summary(1))
        val viewModel = SupportListViewModel(repository)
        viewModel.refresh()

        repository.pages[1] = ticketsPage(1, hasNext = false, summary(2), summary(1))
        viewModel.refresh()

        assertEquals(listOf(2L, 1L), viewModel.content().items.map { it.id })
    }

    @Test
    fun `load more appends next page without duplicates`() {
        repository.pages[1] = ticketsPage(1, hasNext = true, summary(1), summary(2))
        repository.pages[2] = ticketsPage(2, hasNext = false, summary(2), summary(3))
        val viewModel = SupportListViewModel(repository)
        viewModel.refresh()

        viewModel.onLoadMoreClick()

        assertEquals(listOf(1L, 2L, 3L), viewModel.content().items.map { it.id })
        assertFalse(viewModel.content().hasNextPage)
        assertEquals(listOf(1, 2), repository.requestedPages)
    }

    // Новое обращение

    @Test
    fun `new ticket requires subject and message`() {
        val viewModel = SupportNewTicketViewModel(repository)

        viewModel.onSubjectChange("Тема")
        viewModel.onMessageChange("   ")
        viewModel.onSendClick()

        assertFalse(viewModel.state.value.canSend)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `new ticket input is limited like on server`() {
        val viewModel = SupportNewTicketViewModel(repository)

        viewModel.onSubjectChange("а".repeat(300))
        viewModel.onMessageChange("б".repeat(5000))

        assertEquals(255, viewModel.state.value.subject.length)
        assertEquals(4000, viewModel.state.value.message.length)
    }

    @Test
    fun `created ticket id is emitted with trimmed text`() = runTest {
        repository.createResult = DataResult.Success(ticket(42, SupportTicketStatus.Open))
        val viewModel = SupportNewTicketViewModel(repository)
        viewModel.onSubjectChange("  Тема ")
        viewModel.onMessageChange(" Текст  ")

        viewModel.onSendClick()

        assertEquals(42L, viewModel.createdTicketId.first())
        assertEquals(listOf("Тема" to "Текст"), repository.created)
        // Повторное нажатие не создаёт второе обращение.
        viewModel.onSendClick()
        assertEquals(1, repository.created.size)
    }

    @Test
    fun `failed creation keeps form and shows error`() = runTest {
        repository.createResult = DataResult.Failure(DataError.Server)
        val viewModel = SupportNewTicketViewModel(repository)
        viewModel.onSubjectChange("Тема")
        viewModel.onMessageChange("Текст")

        viewModel.onSendClick()

        assertEquals(R.string.data_error_server, viewModel.messages.first())
        assertFalse(viewModel.state.value.isSending)
        assertEquals("Тема", viewModel.state.value.subject)
        assertTrue(viewModel.state.value.canSend)
    }

    // Обращение с перепиской

    @Test
    fun `ticket shows messages and marks author messages`() {
        repository.tickets[5] = ticket(
            5,
            SupportTicketStatus.InProgress,
            message(1, "author"),
            message(2, "support"),
        )

        val viewModel = SupportTicketViewModel(5, repository)

        assertEquals(listOf(true, false), viewModel.content().messages.map { it.isFromAuthor })
        assertTrue(viewModel.content().canReply)
    }

    @Test
    fun `resolved and closed tickets do not accept replies`() {
        repository.tickets[5] = ticket(5, SupportTicketStatus.Resolved)
        repository.tickets[6] = ticket(6, SupportTicketStatus.Closed)

        val resolved = SupportTicketViewModel(5, repository)
        val closed = SupportTicketViewModel(6, repository)
        resolved.onReplyChange("Ещё вопрос")
        resolved.onSendClick()

        assertFalse(resolved.content().canReply)
        assertFalse(closed.content().canReply)
        assertTrue(repository.sent.isEmpty())
    }

    @Test
    fun `sent reply is appended and field is cleared`() {
        repository.tickets[5] = ticket(5, SupportTicketStatus.Open, message(1, "author"))
        repository.sendResult = DataResult.Success(message(2, "author", "Ответ"))
        val viewModel = SupportTicketViewModel(5, repository)

        viewModel.onReplyChange("  Ответ ")
        viewModel.onSendClick()

        assertEquals(listOf(5L to "Ответ"), repository.sent)
        assertEquals(listOf(1L, 2L), viewModel.content().messages.map { it.id })
        assertTrue(viewModel.content().messages.last().isFromAuthor)
        assertEquals("", viewModel.content().reply)
        assertFalse(viewModel.content().isSending)
    }

    @Test
    fun `failed reply keeps text and shows error`() = runTest {
        repository.tickets[5] = ticket(5, SupportTicketStatus.Open)
        repository.sendResult = DataResult.Failure(DataError.Timeout)
        val viewModel = SupportTicketViewModel(5, repository)

        viewModel.onReplyChange("Ответ")
        viewModel.onSendClick()

        assertEquals(R.string.data_error_timeout, viewModel.messages.first())
        assertEquals("Ответ", viewModel.content().reply)
        assertTrue(viewModel.content().messages.isEmpty())
    }

    @Test
    fun `ticket load error and retry`() {
        val viewModel = SupportTicketViewModel(5, repository)
        assertEquals(SupportTicketScreenState.Error(DataError.Server), viewModel.state.value)

        repository.tickets[5] = ticket(5, SupportTicketStatus.Open)
        viewModel.onRetryClick()

        assertEquals(5L, viewModel.content().id)
    }

    @Test
    fun `blank reply is not sent`() {
        repository.tickets[5] = ticket(5, SupportTicketStatus.Open)
        val viewModel = SupportTicketViewModel(5, repository)

        viewModel.onReplyChange("   ")
        viewModel.onSendClick()

        assertTrue(repository.sent.isEmpty())
    }
}