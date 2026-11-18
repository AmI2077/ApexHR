package com.example.apexhr.features.support.data

import com.example.apexhr.features.support.data.dto.SupportMessageDto
import com.example.apexhr.features.support.data.dto.SupportTicketDetailsDto
import com.example.apexhr.features.support.data.dto.SupportTicketSummaryDto
import com.example.apexhr.features.support.data.dto.SupportTicketsPageDto
import com.example.apexhr.features.support.domain.SupportTicketStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportMappersTest {

    @Test
    fun `status is parsed in any case and format`() {
        assertEquals(SupportTicketStatus.Open, "Open".toSupportTicketStatus())
        assertEquals(SupportTicketStatus.InProgress, "InProgress".toSupportTicketStatus())
        assertEquals(SupportTicketStatus.InProgress, "in_progress".toSupportTicketStatus())
        assertEquals(SupportTicketStatus.Resolved, "RESOLVED".toSupportTicketStatus())
        assertEquals(SupportTicketStatus.Closed, "closed".toSupportTicketStatus())
        assertEquals(SupportTicketStatus.Open, null.toSupportTicketStatus())
        assertEquals(SupportTicketStatus.Open, "что-то новое".toSupportTicketStatus())
    }

    @Test
    fun `only open and in progress tickets accept messages`() {
        assertTrue(SupportTicketStatus.Open.acceptsMessages)
        assertTrue(SupportTicketStatus.InProgress.acceptsMessages)
        assertFalse(SupportTicketStatus.Resolved.acceptsMessages)
        assertFalse(SupportTicketStatus.Closed.acceptsMessages)
    }

    @Test
    fun `summary uses last message time and skips items without id`() {
        val page = SupportTicketsPageDto(
            items = listOf(
                SupportTicketSummaryDto(
                    id = 1,
                    subject = "  Тема  ",
                    status = "Closed",
                    messagesCount = 2,
                    lastMessageAt = "2026-11-10T10:00:00Z",
                    updatedAt = "2026-11-01T10:00:00Z",
                ),
                SupportTicketSummaryDto(id = null, subject = "Без id"),
                SupportTicketSummaryDto(id = 2, subject = " ", updatedAt = "2026-11-02T10:00:00"),
            ),
            page = 1,
            totalPages = 2,
        ).toDomain()

        assertEquals(listOf(1L, 2L), page.items.map { it.id })
        assertEquals("Тема", page.items[0].subject)
        assertEquals(Instant.parse("2026-11-10T10:00:00Z"), page.items[0].updatedAt)
        assertNull(page.items[1].subject)
        assertEquals(Instant.parse("2026-11-02T10:00:00Z"), page.items[1].updatedAt)
        assertTrue(page.hasNextPage)
    }

    @Test
    fun `message author falls back to full name`() {
        val withUserName = SupportMessageDto(id = 1, userName = "Иванова Анна", message = "Текст").toDomain()!!
        val withParts = SupportMessageDto(
            id = 2,
            userFirstName = "Анна",
            userLastName = "Иванова",
            userMiddleName = "Петровна",
            message = "Текст",
        ).toDomain()!!
        val anonymous = SupportMessageDto(id = 3, message = "Текст").toDomain()!!

        assertEquals("Иванова Анна", withUserName.authorName)
        assertEquals("Иванова Анна Петровна", withParts.authorName)
        assertNull(anonymous.authorName)
        assertNull(SupportMessageDto(id = 4, message = "  ").toDomain())
    }

    @Test
    fun `ticket messages are deduplicated and sorted by time`() {
        val ticket = SupportTicketDetailsDto(
            id = 10,
            subject = "Тема",
            status = "InProgress",
            userId = "u1",
            messages = listOf(
                SupportMessageDto(id = 2, userId = "s1", message = "Ответ", createdAt = "2026-11-10T11:00:00Z"),
                SupportMessageDto(id = 1, userId = "u1", message = "Вопрос", createdAt = "2026-11-10T10:00:00Z"),
                SupportMessageDto(id = 2, userId = "s1", message = "Ответ", createdAt = "2026-11-10T11:00:00Z"),
            ),
        ).toDomain()!!

        assertEquals(listOf(1L, 2L), ticket.messages.map { it.id })
        assertEquals("u1", ticket.authorId)
        assertEquals(SupportTicketStatus.InProgress, ticket.status)
        assertNull(SupportTicketDetailsDto(id = null).toDomain())
    }
}