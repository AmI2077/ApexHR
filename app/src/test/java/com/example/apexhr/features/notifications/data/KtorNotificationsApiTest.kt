package com.example.apexhr.features.notifications.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.testing.JsonRecordingHttpClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Запросы KtorNotificationsApi; в сеть ничего не уходит. */
class KtorNotificationsApiTest {

    @Test
    fun `notifications are requested with page and page size`() = runBlocking {
        val http = JsonRecordingHttpClient(
            body = """{"items":[],"totalCount":0,"page":2,"pageSize":20,"totalPages":0,"hasNextPage":false}""",
        )

        val page = KtorNotificationsApi(http.client).getNotifications(page = 2, pageSize = 20)

        val request = http.requests.single()
        assertEquals("GET", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/notifications?page=2&pageSize=20", request.url.toString())
        assertEquals(2, page.page)
        assertTrue(page.items.orEmpty().isEmpty())
    }

    @Test
    fun `unread count is parsed`() = runBlocking {
        val http = JsonRecordingHttpClient(body = """{"count":3}""")

        val count = KtorNotificationsApi(http.client).getUnreadCount()

        assertEquals("${NetworkConfig.BASE_URL}/api/notifications/unread-count", http.requests.single().url.toString())
        assertEquals(3, count.count)
    }

    @Test
    fun `mark read uses patch on notification`() = runBlocking {
        val http = JsonRecordingHttpClient()

        KtorNotificationsApi(http.client).markRead(15)

        val request = http.requests.single()
        assertEquals("PATCH", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/notifications/15/read", request.url.toString())
    }

    @Test
    fun `read all uses post`() = runBlocking {
        val http = JsonRecordingHttpClient()

        KtorNotificationsApi(http.client).markAllRead()

        val request = http.requests.single()
        assertEquals("POST", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/notifications/read-all", request.url.toString())
    }

    @Test(expected = Exception::class)
    fun `error status is thrown`() {
        runBlocking {
            KtorNotificationsApi(JsonRecordingHttpClient(code = 500).client).markAllRead()
        }
    }
}
