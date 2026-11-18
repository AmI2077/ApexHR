package com.example.apexhr.features.support.domain

import java.time.Instant

/** Статус обращения в поддержку. */
enum class SupportTicketStatus {
    Open,
    InProgress,
    Resolved,
    Closed;

    /** В решённое и закрытое обращение новые сообщения не принимаются (как в вебе). */
    val acceptsMessages: Boolean
        get() = this == Open || this == InProgress
}

/** Обращение в списке «Мои обращения». */
data class SupportTicketSummary(
    val id: Long,
    val subject: String?,
    val status: SupportTicketStatus,
    val messagesCount: Int,
    /** Время последнего сообщения, а если его нет — последнего изменения или создания. */
    val updatedAt: Instant?,
)

data class SupportTicketsPage(
    val items: List<SupportTicketSummary>,
    val page: Int,
    val hasNextPage: Boolean,
)

/** Сообщение в переписке по обращению. */
data class SupportMessage(
    val id: Long,
    val authorId: String?,
    val authorName: String?,
    val text: String,
    val createdAt: Instant?,
)

/** Обращение с перепиской. */
data class SupportTicket(
    val id: Long,
    val subject: String?,
    val status: SupportTicketStatus,
    /** Автор обращения: по нему его сообщения отличаются от ответов поддержки. */
    val authorId: String?,
    val messages: List<SupportMessage>,
)