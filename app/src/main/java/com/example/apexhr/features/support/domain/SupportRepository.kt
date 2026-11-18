package com.example.apexhr.features.support.domain

import com.example.apexhr.core.domain.DataResult

interface SupportRepository {
    suspend fun getTickets(page: Int, pageSize: Int = PAGE_SIZE): DataResult<SupportTicketsPage>
    suspend fun getTicket(id: Long): DataResult<SupportTicket>
    suspend fun createTicket(subject: String, message: String): DataResult<SupportTicket>
    suspend fun sendMessage(ticketId: Long, message: String): DataResult<SupportMessage>

    companion object {
        const val PAGE_SIZE = 20

        /** Ограничения сервера на тему и текст сообщения. */
        const val SUBJECT_MAX_LENGTH = 255
        const val MESSAGE_MAX_LENGTH = 4000
    }
}