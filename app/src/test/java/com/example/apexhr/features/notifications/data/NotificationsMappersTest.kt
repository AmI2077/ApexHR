package com.example.apexhr.features.notifications.data

import com.example.apexhr.features.notifications.data.dto.NotificationDto
import com.example.apexhr.features.notifications.data.dto.NotificationPageDto
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsMappersTest {

    @Test
    fun `title and body fall back to alternative fields`() {
        val notification = NotificationDto(
            id = 1,
            subject = " Заказ ",
            message = "Оплачен",
            createdAt = "2026-11-18T10:00:00Z",
            read = true,
        ).toDomain()!!

        assertEquals("Заказ", notification.title)
        assertEquals("Оплачен", notification.body)
        assertEquals(Instant.parse("2026-11-18T10:00:00Z"), notification.createdAt)
        assertTrue(notification.isRead)
    }

    @Test
    fun `blank fields become null and unread by default`() {
        val notification = NotificationDto(id = 2, title = " ", text = "", createdAt = "bad").toDomain()!!

        assertNull(notification.title)
        assertNull(notification.body)
        assertNull(notification.createdAt)
        assertFalse(notification.isRead)
    }

    @Test
    fun `date without zone is treated as utc`() {
        assertEquals(
            Instant.parse("2026-11-18T10:00:00Z"),
            "2026-11-18T10:00:00".toNotificationInstantOrNull(),
        )
    }

    @Test
    fun `notifications without id are skipped`() {
        val page = NotificationPageDto(
            items = listOf(NotificationDto(id = null, title = "x"), NotificationDto(id = 3, title = "y")),
            page = 1,
            totalPages = 2,
        ).toDomain()

        assertEquals(listOf(3L), page.items.map { it.id })
        assertTrue(page.hasNextPage)
    }

    @Test
    fun `explicit hasNextPage wins over total pages`() {
        val page = NotificationPageDto(items = emptyList(), page = 1, totalPages = 5, hasNextPage = false).toDomain()

        assertFalse(page.hasNextPage)
    }
}
