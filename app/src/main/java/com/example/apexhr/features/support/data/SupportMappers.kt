package com.example.apexhr.features.support.data

import com.example.apexhr.features.support.data.dto.SupportMessageDto
import com.example.apexhr.features.support.data.dto.SupportTicketDetailsDto
import com.example.apexhr.features.support.data.dto.SupportTicketSummaryDto
import com.example.apexhr.features.support.data.dto.SupportTicketsPageDto
import com.example.apexhr.features.support.domain.SupportMessage
import com.example.apexhr.features.support.domain.SupportTicket
import com.example.apexhr.features.support.domain.SupportTicketStatus
import com.example.apexhr.features.support.domain.SupportTicketSummary
import com.example.apexhr.features.support.domain.SupportTicketsPage
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/** Статус без учёта регистра и разделителей (`InProgress`, `in_progress`); неизвестный — «Открыто», как в вебе. */
fun String?.toSupportTicketStatus(): SupportTicketStatus =
    when (this?.lowercase()?.filter { it.isLetter() }) {
        "inprogress" -> SupportTicketStatus.InProgress
        "resolved" -> SupportTicketStatus.Resolved
        "closed" -> SupportTicketStatus.Closed
        else -> SupportTicketStatus.Open
    }

fun SupportTicketSummaryDto.toDomain(): SupportTicketSummary? {
    val id = id ?: return null
    return SupportTicketSummary(
        id = id,
        subject = subject.cleanText(),
        status = status.toSupportTicketStatus(),
        messagesCount = (messagesCount ?: 0).coerceAtLeast(0),
        updatedAt = lastMessageAt.toSupportInstantOrNull()
            ?: updatedAt.toSupportInstantOrNull()
            ?: createdAt.toSupportInstantOrNull(),
    )
}

fun SupportTicketsPageDto.toDomain(): SupportTicketsPage = SupportTicketsPage(
    items = items.orEmpty().mapNotNull { it.toDomain() },
    page = page,
    hasNextPage = hasNextPage ?: (page < totalPages),
)

/** Сообщение без id или без текста пропускается. */
fun SupportMessageDto.toDomain(): SupportMessage? {
    val id = id ?: return null
    val text = message.cleanText() ?: return null
    return SupportMessage(
        id = id,
        authorId = userId,
        authorName = userName.cleanText() ?: listOf(userLastName, userFirstName, userMiddleName)
            .mapNotNull { it.cleanText() }
            .joinToString(" ")
            .ifEmpty { null },
        text = text,
        createdAt = createdAt.toSupportInstantOrNull(),
    )
}

/** Повторы одного сообщения (одинаковый id) показываем один раз; порядок — по времени. */
fun SupportTicketDetailsDto.toDomain(): SupportTicket? {
    val id = id ?: return null
    return SupportTicket(
        id = id,
        subject = subject.cleanText(),
        status = status.toSupportTicketStatus(),
        authorId = userId,
        messages = messages.orEmpty()
            .mapNotNull { it.toDomain() }
            .distinctBy { it.id }
            .sortedWith(compareBy(nullsFirst()) { it.createdAt }),
    )
}

private fun String?.cleanText(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/** Дата с зоной или без неё; без зоны считаем, что это UTC. */
fun String?.toSupportInstantOrNull(): Instant? {
    val value = cleanText() ?: return null
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value).toInstant(ZoneOffset.UTC) }.getOrNull()
}