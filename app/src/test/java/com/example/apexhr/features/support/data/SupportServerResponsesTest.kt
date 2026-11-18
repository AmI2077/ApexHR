package com.example.apexhr.features.support.data

import com.example.apexhr.core.testing.JsonRecordingHttpClient
import com.example.apexhr.features.support.data.dto.CreateSupportTicketRequest
import com.example.apexhr.features.support.data.dto.SendSupportMessageRequest
import com.example.apexhr.features.support.domain.SupportTicketStatus
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Ответы сервера поддержки в том виде, в каком они пришли при живой проверке (имена изменены). */
class SupportServerResponsesTest {

    private val message = """{"id":33,"userId":"u-1","userFirstName":"Иван","userLastName":"Петров",
        |"userMiddleName":"Сергеевич","message":"Второе сообщение","createdAt":"2026-11-18T15:09:11.9617951Z",
        |"userName":"Иван Сергеевич Петров"}""".trimMargin()

    @Test
    fun `created ticket is parsed`() = runBlocking {
        val http = JsonRecordingHttpClient(
            code = 201,
            body = """{"id":10,"subject":"Тест","status":"Open","userId":"u-1","userFirstName":"Иван",
                |"assignedTo":null,"assignedToName":null,"createdAt":"2026-11-18T15:09:05.03343Z",
                |"updatedAt":"2026-11-18T15:09:05.03343Z","messages":[$message],"userName":"Иван Сергеевич Петров"}""".trimMargin(),
        )

        val ticket = KtorSupportApi(http.client).createTicket(CreateSupportTicketRequest("Тест", "Текст")).toDomain()!!

        assertEquals(10L, ticket.id)
        assertEquals(SupportTicketStatus.Open, ticket.status)
        assertEquals("u-1", ticket.authorId)
        assertEquals("Иван Сергеевич Петров", ticket.messages.single().authorName)
        assertEquals(Instant.parse("2026-11-18T15:09:11.9617951Z"), ticket.messages.single().createdAt)
    }

    @Test
    fun `sent message and list page are parsed`() = runBlocking {
        val sent = KtorSupportApi(JsonRecordingHttpClient(body = message).client)
            .sendMessage(10, SendSupportMessageRequest("Второе сообщение")).toDomain()!!
        assertEquals(33L, sent.id)

        val page = KtorSupportApi(
            JsonRecordingHttpClient(
                body = """{"items":[{"id":10,"subject":"Тест","status":"Open","assignedTo":null,"messagesCount":2,
                    |"lastMessageAt":"2026-11-18T15:09:11.961795Z","createdAt":"2026-11-18T15:09:05.03343Z",
                    |"updatedAt":"2026-11-18T15:09:11.961795Z"}],"totalCount":1,"page":1,"pageSize":20,
                    |"totalPages":1,"hasPreviousPage":false,"hasNextPage":false}""".trimMargin(),
            ).client,
        ).getTickets(1, 20).toDomain()

        assertEquals(2, page.items.single().messagesCount)
        assertEquals(Instant.parse("2026-11-18T15:09:11.961795Z"), page.items.single().updatedAt)
        assertFalse(page.hasNextPage)
    }
}