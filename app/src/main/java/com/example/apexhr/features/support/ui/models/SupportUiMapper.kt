package com.example.apexhr.features.support.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.features.support.domain.SupportMessage
import com.example.apexhr.features.support.domain.SupportTicket
import com.example.apexhr.features.support.domain.SupportTicketStatus
import com.example.apexhr.features.support.domain.SupportTicketSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SUPPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

private fun Instant.formatSupportDate(zone: ZoneId): String = atZone(zone).format(SUPPORT_DATE_FORMAT)

@StringRes
fun SupportTicketStatus.labelRes(): Int = when (this) {
    SupportTicketStatus.Open -> R.string.support_status_open
    SupportTicketStatus.InProgress -> R.string.support_status_in_progress
    SupportTicketStatus.Resolved -> R.string.support_status_resolved
    SupportTicketStatus.Closed -> R.string.support_status_closed
}

/** Пояснение вместо поля ответа у решённого или закрытого обращения. */
@StringRes
fun SupportTicketStatus.closedNoteRes(): Int? = when (this) {
    SupportTicketStatus.Resolved -> R.string.support_resolved_note
    SupportTicketStatus.Closed -> R.string.support_closed_note
    else -> null
}

/** @param zone часовой пояс, в котором показываем даты (по умолчанию — устройства). */
fun SupportTicketSummary.toUiModel(zone: ZoneId = ZoneId.systemDefault()): SupportTicketItemUiModel =
    SupportTicketItemUiModel(
        id = id,
        subject = subject,
        status = status,
        messagesCount = messagesCount,
        updatedAt = updatedAt?.formatSupportDate(zone),
    )

fun SupportMessage.toUiModel(ticketAuthorId: String?, zone: ZoneId = ZoneId.systemDefault()): SupportMessageUiModel =
    SupportMessageUiModel(
        id = id,
        authorName = authorName,
        text = text,
        createdAt = createdAt?.formatSupportDate(zone),
        isFromAuthor = ticketAuthorId != null && authorId.equals(ticketAuthorId, ignoreCase = true),
    )

fun SupportTicket.toUiState(zone: ZoneId = ZoneId.systemDefault()): SupportTicketUiState =
    SupportTicketUiState(
        id = id,
        subject = subject,
        status = status,
        messages = messages.map { it.toUiModel(authorId, zone) },
    )

/** Добавляет следующую страницу, не дублируя обращения, сдвинувшиеся между страницами. */
fun List<SupportTicketItemUiModel>.appendPage(next: List<SupportTicketItemUiModel>): List<SupportTicketItemUiModel> {
    val known = mapTo(HashSet()) { it.id }
    return this + next.filter { it.id !in known }
}