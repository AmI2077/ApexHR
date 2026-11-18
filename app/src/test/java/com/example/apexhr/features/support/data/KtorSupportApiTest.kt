package com.example.apexhr.features.support.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.testing.JsonRecordingHttpClient
import com.example.apexhr.core.testing.bodyText
import com.example.apexhr.features.support.data.dto.CreateSupportTicketRequest
import com.example.apexhr.features.support.data.dto.SendSupportMessageRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Запросы KtorSupportApi; в сеть ничего не уходит. */
class KtorSupportApiTest {

    @Test
    fun `tickets are requested with page and page size`() = runBlocking {
        val http = JsonRecordingHttpClient(
            body = """{"items":[{"id":5,"subject":"Тема","status":"InProgress","messagesCount":2}],"page":1,"totalPages":1,"hasNextPage":false}""",
        )

        val page = KtorSupportApi(http.client).getTickets(page = 1, pageSize = 20)

        val request = http.requests.single()
        assertEquals("GET", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/support/tickets?page=1&pageSize=20", request.url.toString())
        assertEquals(5L, page.items!!.single().id)
        assertEquals("InProgress", page.items!!.single().status)
    }

    @Test
    fun `ticket details are requested by id`() = runBlocking {
        val http = JsonRecordingHttpClient(
            body = """{"id":7,"subject":"Тема","status":"Open","messages":[{"id":1,"message":"Привет"}]}""",
        )

        val ticket = KtorSupportApi(http.client).getTicket(7)

        assertEquals("${NetworkConfig.BASE_URL}/api/support/tickets/7", http.requests.single().url.toString())
        assertEquals("Привет", ticket.messages!!.single().message)
    }

    @Test
    fun `ticket is created with subject and message`() = runBlocking {
        val http = JsonRecordingHttpClient(code = 201, body = """{"id":9,"subject":"Тема","status":"Open"}""")

        val ticket = KtorSupportApi(http.client).createTicket(CreateSupportTicketRequest("Тема", "Текст"))

        val request = http.requests.single()
        assertEquals("POST", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/support/tickets", request.url.toString())
        assertEquals("""{"subject":"Тема","message":"Текст"}""", request.bodyText())
        assertTrue(request.body!!.contentType().toString().startsWith("application/json"))
        assertEquals(9L, ticket.id)
    }

    @Test
    fun `message is posted to ticket messages`() = runBlocking {
        val http = JsonRecordingHttpClient(code = 201, body = """{"id":3,"message":"Ответ"}""")

        val message = KtorSupportApi(http.client).sendMessage(4, SendSupportMessageRequest("Ответ"))

        val request = http.requests.single()
        assertEquals("POST", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/support/tickets/4/messages", request.url.toString())
        assertEquals("""{"message":"Ответ"}""", request.bodyText())
        assertEquals(3L, message.id)
    }

    @Test
    fun `error status throws`() = runBlocking {
        try {
            KtorSupportApi(JsonRecordingHttpClient(code = 400).client)
                .sendMessage(4, SendSupportMessageRequest("Ответ"))
            fail("Ожидалась ошибка")
        } catch (e: ApiStatusException) {
            assertEquals(400, e.statusCode)
        }
    }
}