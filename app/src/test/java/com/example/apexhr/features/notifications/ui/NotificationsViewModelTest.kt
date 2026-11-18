package com.example.apexhr.features.notifications.ui

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.ui.model.LoadMoreState
import com.example.apexhr.features.notifications.domain.AppNotification
import com.example.apexhr.features.notifications.domain.NotificationsPage
import com.example.apexhr.features.notifications.domain.NotificationsRepository
import com.example.apexhr.features.notifications.ui.models.NotificationsScreenState
import com.example.apexhr.features.notifications.ui.models.NotificationsUiState
import kotlinx.coroutines.CompletableDeferred
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
class NotificationsViewModelTest {

    private val repository = FakeNotificationsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun NotificationsViewModel.content(): NotificationsUiState =
        (state.value as NotificationsScreenState.Content).uiState

    @Test
    fun `first page is shown`() {
        repository.pages[1] = page(1, hasNext = true, notification(1), notification(2, read = true))

        val viewModel = NotificationsViewModel(repository)

        assertEquals(listOf(1L, 2L), viewModel.content().items.map { it.id })
        assertTrue(viewModel.content().hasNextPage)
        assertTrue(viewModel.content().hasUnread)
    }

    @Test
    fun `load error shows error state and retry reloads`() {
        val viewModel = NotificationsViewModel(repository)
        assertEquals(NotificationsScreenState.Error(DataError.Network), viewModel.state.value)

        repository.pages[1] = page(1, hasNext = false, notification(1))
        viewModel.onRetryClick()

        assertEquals(listOf(1L), viewModel.content().items.map { it.id })
    }

    @Test
    fun `load more appends next page without duplicates`() {
        repository.pages[1] = page(1, hasNext = true, notification(1), notification(2))
        repository.pages[2] = page(2, hasNext = false, notification(2), notification(3))
        val viewModel = NotificationsViewModel(repository)

        viewModel.onLoadMoreClick()

        assertEquals(listOf(1L, 2L, 3L), viewModel.content().items.map { it.id })
        assertFalse(viewModel.content().hasNextPage)
        assertFalse(viewModel.content().isLoadingMore)
        assertEquals(listOf(1, 2), repository.requestedPages)
    }

    @Test
    fun `load more error stays in list footer and retry loads the page`() = runTest {
        repository.pages[1] = page(1, hasNext = true, notification(1))
        val viewModel = NotificationsViewModel(repository)

        viewModel.onLoadMoreClick()

        assertEquals(listOf(1L), viewModel.content().items.map { it.id })
        assertTrue(viewModel.content().hasNextPage)
        assertEquals(LoadMoreState.Error(DataError.Network), viewModel.content().loadMore)

        repository.pages[2] = page(2, hasNext = false, notification(2))
        viewModel.onLoadMoreClick()

        assertEquals(listOf(1L, 2L), viewModel.content().items.map { it.id })
        assertEquals(LoadMoreState.Hidden, viewModel.content().loadMore)
    }

    @Test
    fun `click marks notification read immediately`() {
        repository.pages[1] = page(1, hasNext = false, notification(1), notification(2))
        val pending = CompletableDeferred<DataResult<Unit>>()
        repository.markReadResult = pending
        val viewModel = NotificationsViewModel(repository)

        viewModel.onNotificationClick(1)

        assertTrue(viewModel.content().items.first { it.id == 1L }.isRead)
        assertFalse(viewModel.content().items.first { it.id == 2L }.isRead)
        pending.complete(DataResult.Success(Unit))
        assertTrue(viewModel.content().items.first { it.id == 1L }.isRead)
        assertEquals(listOf(1L), repository.markedRead)
    }

    @Test
    fun `failed mark read is rolled back`() = runTest {
        repository.pages[1] = page(1, hasNext = false, notification(1))
        repository.markReadResult = CompletableDeferred(DataResult.Failure(DataError.Server))
        val viewModel = NotificationsViewModel(repository)

        viewModel.onNotificationClick(1)

        assertFalse(viewModel.content().items.single().isRead)
        assertEquals(R.string.notifications_error_read, viewModel.messages.first())
    }

    @Test
    fun `click on read notification does not call server`() {
        repository.pages[1] = page(1, hasNext = false, notification(1, read = true))
        val viewModel = NotificationsViewModel(repository)

        viewModel.onNotificationClick(1)

        assertTrue(repository.markedRead.isEmpty())
    }

    @Test
    fun `read all marks every notification`() {
        repository.pages[1] = page(1, hasNext = false, notification(1), notification(2))
        val viewModel = NotificationsViewModel(repository)

        viewModel.onReadAllClick()

        assertFalse(viewModel.content().hasUnread)
        assertFalse(viewModel.content().isMarkingAll)
        assertEquals(1, repository.readAllCalls)
    }

    @Test
    fun `read all failure keeps unread`() = runTest {
        repository.pages[1] = page(1, hasNext = false, notification(1))
        repository.readAllResult = DataResult.Failure(DataError.Timeout)
        val viewModel = NotificationsViewModel(repository)

        viewModel.onReadAllClick()

        assertTrue(viewModel.content().hasUnread)
        assertEquals(R.string.notifications_error_read, viewModel.messages.first())
    }

    @Test
    fun `read all is skipped when nothing unread`() {
        repository.pages[1] = page(1, hasNext = false, notification(1, read = true))
        val viewModel = NotificationsViewModel(repository)

        viewModel.onReadAllClick()

        assertEquals(0, repository.readAllCalls)
    }

    @Test
    fun `badge shows unread count and keeps it on error`() {
        repository.unreadCount = DataResult.Success(4)
        val badge = NotificationsBadgeViewModel(repository)

        badge.refresh()
        assertEquals(4, badge.unreadCount.value)

        repository.unreadCount = DataResult.Failure(DataError.Network)
        badge.refresh()
        assertEquals(4, badge.unreadCount.value)
    }
}

private fun notification(id: Long, read: Boolean = false) =
    AppNotification(id = id, title = "Уведомление $id", body = null, createdAt = null, isRead = read)

private fun page(number: Int, hasNext: Boolean, vararg items: AppNotification) =
    NotificationsPage(items = items.toList(), page = number, hasNextPage = hasNext)

private class FakeNotificationsRepository : NotificationsRepository {
    val pages = mutableMapOf<Int, NotificationsPage>()
    val requestedPages = mutableListOf<Int>()
    val markedRead = mutableListOf<Long>()
    var markReadResult = CompletableDeferred<DataResult<Unit>>(DataResult.Success(Unit))
    var readAllResult: DataResult<Unit> = DataResult.Success(Unit)
    var readAllCalls = 0
    var unreadCount: DataResult<Int> = DataResult.Success(0)

    override suspend fun getNotifications(page: Int, pageSize: Int): DataResult<NotificationsPage> {
        requestedPages += page
        return pages[page]?.let { DataResult.Success(it) } ?: DataResult.Failure(DataError.Network)
    }

    override suspend fun getUnreadCount(): DataResult<Int> = unreadCount

    override suspend fun markRead(id: Long): DataResult<Unit> {
        markedRead += id
        return markReadResult.await()
    }

    override suspend fun markAllRead(): DataResult<Unit> {
        readAllCalls++
        return readAllResult
    }
}
