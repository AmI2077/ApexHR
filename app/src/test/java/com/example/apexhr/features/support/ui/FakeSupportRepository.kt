package com.example.apexhr.features.support.ui

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.support.domain.SupportMessage
import com.example.apexhr.features.support.domain.SupportRepository
import com.example.apexhr.features.support.domain.SupportTicket
import com.example.apexhr.features.support.domain.SupportTicketStatus
import com.example.apexhr.features.support.domain.SupportTicketSummary
import com.example.apexhr.features.support.domain.SupportTicketsPage

/** Репозиторий поддержки в памяти; без заданного ответа возвращает ошибку сети. */
class FakeSupportRepository : SupportRepository {
    val pages = mutableMapOf<Int, SupportTicketsPage>()
    val tickets = mutableMapOf<Long, SupportTicket>()
    var createResult: DataResult<SupportTicket> = DataResult.Failure(DataError.Network)
    var sendResult: DataResult<SupportMessage> = DataResult.Failure(DataError.Network)

    val requestedPages = mutableListOf<Int>()
    val created = mutableListOf<Pair<String, String>>()
    val sent = mutableListOf<Pair<Long, String>>()

    override suspend fun getTickets(page: Int, pageSize: Int): DataResult<SupportTicketsPage> {
        requestedPages += page
        return pages[page]?.let { DataResult.Success(it) } ?: DataResult.Failure(DataError.Network)
    }

    override suspend fun getTicket(id: Long): DataResult<SupportTicket> =
        tickets[id]?.let { DataResult.Success(it) } ?: DataResult.Failure(DataError.Server)

    override suspend fun createTicket(subject: String, message: String): DataResult<SupportTicket> {
        created += subject to message
        return createResult
    }

    override suspend fun sendMessage(ticketId: Long, message: String): DataResult<SupportMessage> {
        sent += ticketId to message
        return sendResult
    }
}

fun summary(id: Long, status: SupportTicketStatus = SupportTicketStatus.Open) =
    SupportTicketSummary(id = id, subject = "Тема $id", status = status, messagesCount = 1, updatedAt = null)

fun ticketsPage(page: Int, hasNext: Boolean, vararg items: SupportTicketSummary) =
    SupportTicketsPage(items = items.toList(), page = page, hasNextPage = hasNext)

fun message(id: Long, authorId: String, text: String = "Сообщение $id") =
    SupportMessage(id = id, authorId = authorId, authorName = null, text = text, createdAt = null)

fun ticket(id: Long, status: SupportTicketStatus, vararg messages: SupportMessage) =
    SupportTicket(id = id, subject = "Тема", status = status, authorId = "author", messages = messages.toList())